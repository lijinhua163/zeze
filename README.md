# zeze

zeze ��һ��֧�������Ӧ�ÿ�ܡ�
��Ӧ�õ����ݷ�����������ִ�С�
����Ҳ�ṩ��һ���򵥵������ܡ�

#### ��װ�̳�

Zeze ��һ����⣬���еĺ��Ĺ��ܶ�������һ���֪��c#�������ô����Ӧ�á�
Gen ��һ������̨���򡣱�����Ժ��������ɴ��롣
GlobalCacheManager ��һ������̨���򡣱�������м��ɡ�

#### ʹ��˵��

1. �����Լ��������������ݣ������������͡�Э�顢���ݿ����ȡ�
   �ο���UnitTest\solution.xml
2. ʹ�� Gen ���ɴ��롣
3. �����ɵ�Module���У�ʵ��Ӧ��Э�飬ʹ�����ݿ����������ݵȡ�
4. ���á�
   �ο���UnitTest\zeze.xml
   һ����˵����ʼ�Ļ�����Ҫ�ṩһ�����ݿ����á����ṩ���õĻ������ݿ����ڴ�ġ�

#### ����ģʽ

1. �����ݿ�֧��
   �ṩ��� DatabaseConf ���á�������ݿ���Ҫ�ò�ͬ Name ���֡�
   Ȼ���� TableConf ��ʹ������ DatabaseName �ѱ�����䵽ĳ�����ݿ��С�
   ���òο���UnitTest\zeze.xml

2. ���ϵ����ݿ���װ������
   ��ʹ��ĳЩǶ��ʽ���ݿ⣨����bdb��ʱ�����ĳ�����ݿ��ļ��ܴ󣬵��ǻ�Ծ���ݿ����ֲ��࣬ÿ�α������ȽϷ�ʱ��
   ���Կ��ǰѱ����Ƶ��µ����ݿ⣬Ȼ��ϵͳ���¿����Ҳ�����¼ʱ���Զ����Ͽ���װ�����ݡ�
   �������Ͽ���ֻ���ģ�����ÿ�α��ݡ�
   TableConf ��ʹ������ DatabaseOldName ָ���ϵ����ݿ⣬������ DatabaseOldMode ��Ϊ 1������Ҫʱ��Zeze �ͻ��Զ����Ͽ���װ�ؼ�¼��
   ���òο���UnitTest\zeze.xml

3. ��� Zeze.Application ֮�������
   һ����˵�������������һ�� Zeze.Application �����ݿ����
   �����Ҫ�ڶ�� Zeze.Application ֮��֧������Ӧ��ֱ�ӷ��ʲ�ͬ App.Module ����ı��񼴿��������֧�֡�
   �������������ύ(Checkpoint)Ĭ������һ�� Zeze.Application ��ִ�еģ�Ϊ���������ύҲԭ�ӻ�����Ҫ��App.Startǰ����ͳһCheckpoint��
   ���ô������ӣ�

   Zeze.Checkpoint checkpoint = new Zeze.Checkpoint();
   // �Ѷ��App�����ݿ���뵽Checkpoint�С�
   checkpoint.Add(demo1.App.Zeze.Databases.Values);
   checkpoint.Add(demo2.App.Zeze.Databases.Values);
   // ����App��Checkpoint��
   demo1.App.Zeze.Checkpoint = checkpoint;
   demo2.App.Zeze.Checkpoint = checkpoint;
   // ����App������������ǰ���á�
   demo1.App.Start();
   demo2.App.Start();

4. Cacheͬ������� Zeze.Application ����ͬһ��������ݿ�
   һ���ģʽ�Ǻ�����ݿ����һ�� Zeze.Application ���ʡ������Ҫ���App����һ�����ݿ⣬��Ҫ����Cacheͬ�����ܡ�
   1) ���� GlobalCacheManager
   2) ���� zeze.xml �����ԣ�GlobalCacheManagerHostNameOrAddress="127.0.0.1" GlobalCacheManagerPort="5555"
      ���òο���UnitTest\zeze.xml
   *) ע�⣬��֧�ֶ��ʹ��ͬһ�� GlobalCacheManager ͬ����Cache�� Zeze.Application ֮������񡣲μ�����ĵ�3�㡣
      ��Ϊ Cache ͬ����Ҫͬ����¼�ĳ���״̬�������ʱ Application ʹ��ͬһ�� Checkpoint����¼ͬ������Ҫ�ȴ��Լ�����������