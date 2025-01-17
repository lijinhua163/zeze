
// ZEZE_FILE_CHUNK {{{ IMPORT GEN
import { Zeze } from "zeze"
import { demo_Module1_Protocol1, demo_Module1_Protocol3, demo_Module1_Rpc1, demo_Module1_Rpc2, } from "gen"
import { demo_App } from "demo/App"
// ZEZE_FILE_CHUNK }}} IMPORT GEN

export class demo_Module1 {
        // ZEZE_FILE_CHUNK {{{ MODULE ENUMS
        // ZEZE_FILE_CHUNK }}} MODULE ENUMS
    public constructor(app: demo_App) {
        // ZEZE_FILE_CHUNK {{{ REGISTER PROTOCOL
        app.Client.FactoryHandleMap.set(7370347356n, new Zeze.ProtocolFactoryHandle(() => { return new demo_Module1_Protocol1(); }, this.ProcessProtocol1.bind(this)));
        app.Client.FactoryHandleMap.set(7815467220n, new Zeze.ProtocolFactoryHandle(() => { return new demo_Module1_Protocol3(); }, this.ProcessProtocol3.bind(this)));
        app.Client.FactoryHandleMap.set(5635082623n, new Zeze.ProtocolFactoryHandle(() => { return new demo_Module1_Rpc1(); }, null));
        app.Client.FactoryHandleMap.set(7854078040n, new Zeze.ProtocolFactoryHandle(() => { return new demo_Module1_Rpc2(); }, this.ProcessRpc2Request.bind(this)));
        // ZEZE_FILE_CHUNK }}} REGISTER PROTOCOL
    }

    public Start(app: demo_App): void {
    }

    public Stop(app: demo_App): void {
    }

    public ProcessProtocol1(protocol: demo_Module1_Protocol1): number {
        return 0;
    }

    public ProcessProtocol3(protocol: demo_Module1_Protocol3): number {
        return 0;
    }

    public ProcessRpc2Request(rpc: demo_Module1_Rpc2): number {
        return 0;
    }

}
