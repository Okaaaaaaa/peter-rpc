package com.peter.extension;

import com.peter.utils.StringUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExtensionLoader<T> {
    // 要扫描的路径名
    private static final String SERVICE_DIRECTORY = "META-INF/extensions/";

    // 接口类型
    private final Class<?> type;

    /**
     * 静态变量
     */
    // (类 -> 类加载器) 缓存
    private static final Map<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>();
    // (类 -> 实例对象) 缓存
    private static final Map<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<>();

    /**
     * 实例变量（每个接口的ExtensionLoader都有自己的一份cachedInstances & cachedClasses）
     */
    // (实例名 -> 实例对象) 缓存
    private final Map<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<>();
    // (类名 -> 类对象) 缓存
    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>();


    private ExtensionLoader(Class<?> type){
        this.type = type;
    }

    /**
     * 获取type类型的 ExtensionLoader 实例
     */
    public static <S> ExtensionLoader<S> getExtensionLoader(Class<S> type){
        if(type == null){
            throw new IllegalArgumentException("Extension type should not be null.");
        }

        // 接口类型
        if(!type.isInterface()){
            throw new IllegalArgumentException("Extension type(" + type + ") is not interface!");
        }

        // 该接口被@SPI标注
        if(type.getAnnotation(SPI.class) == null){
            throw new IllegalArgumentException("Extension type must be annotated by @SPI");
        }

        // 在缓存中查找该类的类加载器
        ExtensionLoader<S> extensionLoader = (ExtensionLoader<S>)EXTENSION_LOADERS.get(type);
        // 没找到，则新建该类的加载器
        if(extensionLoader == null){
            EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader<S>(type));
            extensionLoader = (ExtensionLoader<S>)EXTENSION_LOADERS.get(type);
        }
        return extensionLoader;
    }

    /**
     * 获取name的扩展实例（如JsonSerializer、KryoSerializer）
     */
    public T getExtensionInstance(String name){
        if(StringUtil.isBlank(name)){
            throw new IllegalArgumentException("Extension name should not be null or empty.");
        }
        Holder<Object> holder = cachedInstances.get(name);
        if(holder == null){
            cachedInstances.putIfAbsent(name, new Holder<>());
            holder = cachedInstances.get(name);
        }

        Object instance = holder.get();
        if(instance == null){
            synchronized (holder){
                instance = holder.get();
                if(instance == null){
                    instance = createExtensionInstance(name);
                    holder.set(instance);
                }
            }
        }
        return (T) instance;
    }


    /**
     * 创建 name 的扩展实例
     */
    private T createExtensionInstance(String name){
        // 是否注册了对应的扩展类
        Class<?> clazz = getExtensionClass().get(name);
        if(clazz == null){
            throw new RuntimeException("No such extension of name " + name);
        }

        T instance = (T) EXTENSION_INSTANCES.get(clazz);
        if(instance == null){
            try{
                EXTENSION_INSTANCES.putIfAbsent(clazz, clazz.newInstance());
                instance = (T) EXTENSION_INSTANCES.get(clazz);
            } catch (InstantiationException | IllegalAccessException e) {
                System.out.println(e.getMessage());
            }
        }
        return instance;
    }

    /**
     * 获取所有扩展类
     */
    private Map<String, Class<?>> getExtensionClass(){
        Map<String, Class<?>> classes = cachedClasses.get();
        // 双重校验锁
        if(classes == null || classes.isEmpty()){
            synchronized (cachedClasses){
                classes = cachedClasses.get();
                if(classes == null || classes.isEmpty()){
                    classes = new HashMap<>();
                    loadDirectory(classes);
                    cachedClasses.set(classes);
                }
            }
        }
        return classes;
    }

    /**
     * 根据typeName，调用loadResources加载指定目录下的类
     */
    private void loadDirectory(Map<String, Class<?>> extensionClasses){
        // type: Serializer => typeName: com.peter.serializer.Serializer
        String fileName = ExtensionLoader.SERVICE_DIRECTORY + type.getName();
        try{
            Enumeration<URL> urls;
            ClassLoader classLoader = ExtensionLoader.class.getClassLoader();
            urls = classLoader.getResources(fileName);
            if(urls != null){
                while (urls.hasMoreElements()){
                    URL resourceUrl = urls.nextElement();
                    loadResources(extensionClasses, classLoader, resourceUrl);
                }
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * 在 META-INF/extensions 下的指定文件中加载类，并放入map中
     */
    private void loadResources(Map<String, Class<?>> extensionClasses, ClassLoader classLoader, URL resourceUrl){
        // 读取对应文件中的每一行
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceUrl.openStream(), StandardCharsets.UTF_8))){
            String line;
            while ((line = reader.readLine()) != null){
                // 去掉注释内容
                final int hashtagIdx = line.indexOf("#");
                if(hashtagIdx >= 0){
                    line = line.substring(0, hashtagIdx);
                }
                // 修剪首尾空格
                line = line.trim();

                // kryo=github.javaguide.serialize.kryo.KryoSerializer
                if(!line.isEmpty()){
                    try{
                        final int equalIdx = line.indexOf("=");
                        String name = line.substring(0, equalIdx).trim();
                        String className = line.substring(equalIdx+1).trim();
                        if(!name.isEmpty() && !className.isEmpty()){
                            Class<?> clazz = classLoader.loadClass(className);
                            extensionClasses.put(name, clazz);
                        }
                    } catch (ClassNotFoundException e) {
                        System.out.println("加载类失败："+ e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
