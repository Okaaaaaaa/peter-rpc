package client.rpc_client;

import common.RPCRequest;
import common.RPCResponse;

public interface RPCClient {
    RPCResponse sendRequest(RPCRequest request);
}
