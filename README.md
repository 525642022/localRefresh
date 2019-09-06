网络拦截插件实现

###实现目标：
1.拦截网络请求中所需要的信息
2.可以在拦截过程显示log
###实现基础：
现在的实现都是基于okhttp的基础上
###拦截数据
1.请求url
2.请求参数
3.请求头包含的信息
4.本次请求所花费的时间
5.请求返回的参数
6.请求返回额的code值
###实现原理
我们只需要在Okhttp初始化的时候插入自己的拦截器就行了
我们先看一下okhttp的builder

```
 class Builder constructor() {
    internal var dispatcher: Dispatcher = Dispatcher()
    internal var connectionPool: ConnectionPool = ConnectionPool()
    internal val interceptors: MutableList<Interceptor> = mutableListOf()
    internal val networkInterceptors: MutableList<Interceptor> = mutableListOf()
    internal var eventListenerFactory: EventListener.Factory = EventListener.NONE.asFactory()
    internal var retryOnConnectionFailure = true
    internal var authenticator: Authenticator = Authenticator.NONE
    internal var followRedirects = true
    internal var followSslRedirects = true
    internal var cookieJar: CookieJar = CookieJar.NO_COOKIES
    internal var cache: Cache? = null
    internal var dns: Dns = Dns.SYSTEM
    internal var proxy: Proxy? = null
    internal var proxySelector: ProxySelector? = null
    internal var proxyAuthenticator: Authenticator = Authenticator.NONE
    internal var socketFactory: SocketFactory = SocketFactory.getDefault()
    internal var sslSocketFactoryOrNull: SSLSocketFactory? = null
    internal var x509TrustManagerOrNull: X509TrustManager? = null
    internal var connectionSpecs: List<ConnectionSpec> = DEFAULT_CONNECTION_SPECS
    internal var protocols: List<Protocol> = DEFAULT_PROTOCOLS
    internal var hostnameVerifier: HostnameVerifier = OkHostnameVerifier
    internal var certificatePinner: CertificatePinner = CertificatePinner.DEFAULT
    internal var certificateChainCleaner: CertificateChainCleaner? = null
    internal var callTimeout = 0
    internal var connectTimeout = 10_000
    internal var readTimeout = 10_000
    internal var writeTimeout = 10_000
    internal var pingInterval = 0
```

我们所需要多的就是在这里插入自己所需要的拦截器
###实现方式
因为我们要做的是一个可插拔是的插件去统计 
在这里我们一共使用两种方式去实现
1.Aspect
2.gradle plugin+ asm

###具体实现
####Aspect 方式
1.在application的gradle 导入 一个已经封装好的包
      classpath 'com.hujiang.aspectjx:gradle-android-plugin-aspectjx:2.0.4'
2.app的gradle添加引用
apply plugin: 'com.hujiang.android-aspectjx'
3.在application新建一个library 注意：需要在新建的library中引用okhttp
4.在library中添加

```
/**
 * OkHttp 切入点，Http 相关的数据指标在此处进行采集
 */
@Aspect
public class HttpAspect {
     //此处表示我们需要拦截的方法为 OkHttpClient.Builder
    @Around("execution(* okhttp3.OkHttpClient.Builder.build())")
    public Object aroundBuild(ProceedingJoinPoint joinPoint) throws Throwable {
        //拦截以后我们需要惊醒的操作卸载这里
        Object target = joinPoint.getTarget();
        if (target instanceof OkHttpClient.Builder) {
            OkHttpClient.Builder builder = (OkHttpClient.Builder) target;
            //为okhttp添加一个我们自定义的拦截器（拦截器自己实现吧就不贴了）
            builder.addInterceptor(new NetWorkInterceptor());
        }
        return joinPoint.proceed();
    }
}
```

5.在拦截器中收集自己所需要的信息
6.根据已经设置的阈值判断上传信息（暂时还没写）
7.app引入该library 
   implementation project(path: ':ApmHttp')
####gradle plugin+ asm
1.新建一个java library
2.修改gradle

```
apply plugin: 'maven'
apply plugin: 'groovy'



uploadArchives {
    repositories {
        mavenDeployer {//用于发布本地maven
            repository(url: uri("../repos")) // repos 为本地的地址，后续可以替换为网路上的 maven 库地址
            pom.groupId = "com.okhttp.plugin"
            pom.artifactId = "okhttpplugin"
            pom.version = "1.0.0"
        }
    }
}

dependencies {
    compile gradleApi() //gradle sdk
    compile localGroovy() //groovy sdk
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    implementation 'com.android.tools.build:gradle:3.4.2'
    implementation 'com.android.tools.build:gradle-api:3.4.2'
    implementation 'com.quinn.hunter:hunter-transform:0.9.3'
}
```

2.添加一个插件入口OkhttpPlugin 
 并添加resources/META-INF/gradle-plugins/com.okhttp.plugin.properties‘
并在这里面为plugin指定入口

```
package com.okhttp.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

public class OkhttpPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        AppExtension appExtension = (AppExtension)project.getProperties().get("android");
        appExtension.registerTransform(new OkHttpTransform(project), Collections.EMPTY_LIST);
    }
}
```

```
implementation-class=com.okhttp.plugin.OkhttpPlugin
```

3.实现自己的Transform
使用HunterTransform我们可以不用关心transform具体实现的过程只需要设置OkHttpExtension外部变量
和OkHttpWeaver即可
final class OkHttpTransform extends HunterTransform {

```
    private Project project;
    private OkHttpExtension okHttpExtension;

    public OkHttpTransform(Project project) {
        super(project);
        this.project = project;
        project.getExtensions().create("okHttpExt", OkHttpExtension.class);
        this.bytecodeWeaver = new com.okhttp.plugin.bytecode.OkHttpWeaver();
    }

    @Override
    public void transform(Context context, Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs, TransformOutputProvider outputProvider, boolean isIncremental) throws IOException, TransformException, InterruptedException {
        okHttpExtension = (OkHttpExtension) project.getExtensions().getByName("okHttpExt");
        this.bytecodeWeaver.setExtension(okHttpExtension);
        super.transform(context, inputs, referencedInputs, outputProvider, isIncremental);
    }

    @Override
    protected RunVariant getRunVariant() {
        return okHttpExtension.runVariant;
    }

    @Override
    protected boolean inDuplcatedClassSafeMode() {
        return okHttpExtension.duplcatedClassSafeMode;
    }
}
```

4.实现OkHttpWeaver与自定义实现的Transform方法关联的
只需要覆写并实现wrapClassWriter方法 返回相应自定义的ClassVisitor即可

```
public final class OkHttpWeaver extends BaseWeaver {

    private OkHttpExtension okHttpExtension;

    @Override
    public void setExtension(Object extension) {
        if(extension == null) return;
        this.okHttpExtension = (OkHttpExtension) extension;
    }

    @Override
    protected ClassVisitor wrapClassWriter(ClassWriter classWriter) {
        return new OkHttpClassAdapter(classWriter, this.okHttpExtension.weaveEventListener);
    }

}
```

5.实现OkHttpClassAdapter在visitMethod里面我们指定需要修改的方法

```
public final class OkHttpClassAdapter extends ClassVisitor {

    private String className;

    private boolean weaveEventListener;

    OkHttpClassAdapter(final ClassVisitor cv, boolean weaveEventListener) {
        super(Opcodes.ASM5, cv);
        this.weaveEventListener = weaveEventListener;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.className = name;
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name,
                                     final String desc, final String signature, final String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        if(className.equals("okhttp3/OkHttpClient$Builder") && name.equals("<init>")) {
            return mv == null ? null : new OkHttpMethodAdapter(access, desc, mv, weaveEventListener);
        } else {
            return mv;
        }
    }
```

6.实现OkHttpMethodAdapter在visitInsn完成真正的修改 既添加拦截器

```
public final class OkHttpMethodAdapter extends LocalVariablesSorter implements Opcodes {

    private static final LoggerWrapper logger = LoggerWrapper.getLogger(OkHttpMethodAdapter.class);

    private boolean weaveEventListener;

    OkHttpMethodAdapter(int access, String desc, MethodVisitor mv, boolean weaveEventListener) {
        super(Opcodes.ASM5, access, desc, mv);
        this.weaveEventListener = weaveEventListener;
    }

    @Override
    public void visitInsn(int opcode) {
        if ((opcode >= IRETURN && opcode <= RETURN) || opcode == ATHROW) {
            //Interceptor
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, "okhttp3/OkHttpClient$Builder", "interceptors", "Ljava/util/List;");
            mv.visitTypeInsn(NEW, "com/okhttp/library/NetWorkInterceptor");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "com/okhttp/library/NetWorkInterceptor", "<init>", "()V", false);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true);
            mv.visitInsn(POP);
        }
        super.visitInsn(opcode);
    }

}
```

这个样子我们就为我们我们的应用实现了拦截器的插件了
7.具体使用方式
（1）Application 添加应用插件
 classpath 'com.okhttp.plugin:okhttpplugin:1.0.0'
（2）app添加插件引用 apply plugin: 'com.okhttp.plugin'
（3）添加拦截器所在的library     implementation project(path: ':okhttplibrary')
 ###最后加一条
网络拦截实现了 图片拦截也可以实现（在这里之针对glide（因为这个自己用的多））
其实这个实现特别的简答我们只需要吧glide的请求方式设置的okhttpClient 就行了
具体设置方式：在app的gradle中引入   

```
 implementation "com.github.bumptech.glide:okhttp3-integration:4.9.0"
```

就行了剩下的不用操作
###部分优化
减少统计统计字段 ，因为数据是需要本地保存和上传的 ，而输出的具体的请求参数和返回参数的数据太多不利于保存，浏览 ，和后续的上传工作，所以进行部分优化
（1）删除的具体的统计字段，如请求头，请求体，返回的具体参数
（2）为了可以量化过程，添加字段 请求体的数据大小和返回体的数据大小 
