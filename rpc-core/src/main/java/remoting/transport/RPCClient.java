package remoting.transport;

import remoting.dto.RPCRequest;
import remoting.dto.RPCResponse;

public interface RPCClient {
    RPCResponse sendRequest(RPCRequest request);
}
