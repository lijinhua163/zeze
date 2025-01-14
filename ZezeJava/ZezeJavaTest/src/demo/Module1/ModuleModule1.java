package demo.Module1;

// ZEZE_FILE_CHUNK {{{ IMPORT GEN
// ZEZE_FILE_CHUNK }}} IMPORT GEN

public class ModuleModule1 extends AbstractModule {
    public void Start(demo.App app) {
    }

    public void Stop(demo.App app) {
    }

    @Override
    protected long ProcessProtocol3(Protocol3 p) throws Throwable {
        return Zeze.Transaction.Procedure.NotImplement;
    }

    @Override
    protected long ProcessProtocol4(Protocol4 p) throws Throwable {
        return Zeze.Transaction.Procedure.NotImplement;
    }

    @Override
    protected long ProcessRpc1Request(Rpc1 r) throws Throwable {
        return Zeze.Transaction.Procedure.NotImplement;
    }

    public tflush getTflush() {
        return _tflush;
    }

    public Table1 getTable1() {
		return _Table1;
	}

	public Table2 getTable2() {
		return _Table2;
	}

	public Table3 getTable3() {
		return _Table3;
	}

	public TableImportant getTableImportant() {
		return _TableImportant;
	}

    @Override
    public long ProcessProtocolNoProcedure(ProtocolNoProcedure p) {
        return Zeze.Transaction.Procedure.NotImplement;
    }

	// ZEZE_FILE_CHUNK {{{ GEN MODULE
    public ModuleModule1(demo.App app) {
        super(app);
    }
    // ZEZE_FILE_CHUNK }}} GEN MODULE

}
