package ClientZezex.Linkd;

public class ModuleLinkd extends AbstractModule {
    public void Start(ClientGame.App app) throws Throwable {
    }

    public void Stop(ClientGame.App app) throws Throwable {
    }

    @Override
    protected long ProcessKeepAlive(ClientZezex.Linkd.KeepAlive p) {
        return Zeze.Transaction.Procedure.NotImplement;
    }

    // ZEZE_FILE_CHUNK {{{ GEN MODULE @formatter:off
    public ModuleLinkd(ClientGame.App app) {
        super(app);
    }
    // ZEZE_FILE_CHUNK }}} GEN MODULE @formatter:on
}
