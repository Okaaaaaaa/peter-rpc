package com.peter.remoting.transport;

import com.peter.remoting.dto.RPCRequest;
import com.peter.remoting.dto.RPCResponse;

public interface RPCClient {
    RPCResponse sendRequest(RPCRequest request);
}
