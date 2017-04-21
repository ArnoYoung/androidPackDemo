
[toc]
## 简介
![3ac8c26a-0fa1-4bc2-b12a-d9c78d7c0a6e.png](http://upload-images.jianshu.io/upload_images/1426260-d3508a9a52638dee.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
以前了解一些自动化打包，最近要分享，根据[官方](https://developer.android.com/studio/build/build-variants.html)和一些参考文档，做一下简单的总结，简单做了张图，知识点文章都会介绍到。

### build生成apk方式
1.android sutidio 菜单栏，build-》generate signed apk 随后可以选择编译方式和产品风味  
2.在android根目录下命令行
* `./gradlew assemble{BuildType}` 对应编译方式所有productflavor都会输出
* `./gradlew assemble{productFlavor}{BuildType}`  指定构建体输出文件
* `./gradlew assemble `全部构建变体都会输出。

PS：打产品包用Release方式
*  `./gradlew assembleRelease` 或者简写`./gradlew aR`打包所有产品风味的包
*  `./gradlew assemblerelease_normalRelease`指定产生release_normal产品风味的包
### install方式
1.android sutidio 直接菜单栏的Run绿色按钮。左下脚菜单Build Variants，可以选择不同的构建变体  
2.在android目录下运行命令
`./gradlew install{productFlavor}{buildType}`
注意：这种方式如果build自增体现在输出的apk文件名上，可能会报找不到apk文件的错误。


## 签名不在版本控制中
正常情况下会在`build.gradle` 下面配置签名文件的位置和密码，但是签名文件和密码会在版本控制中，不安全，至少密码在版本控制中是不安全的。
推荐两种方式  
1.自定义存储签名密码文件，并自定义函数读取签名密码和签名文件位置。  
2.在本地`~/.gradle/gradle.properties`全局文件中配置签名密码和签名文件位置  

### 自定义读取签名密码文件
单独建立一个签名密码的文件，在工程目录下新建`signingfiles`文件夹，在里面存储签名文件和签名密码文件
* keystore.properties
* guanmai-release-key.keystore

签名密码文件
```
STORE_FILE=../signingfiles/guanmai-release-key.keystore
KEY_ALIAS=guanmai-key-alias
STORE_PASSWORD=guanmai
KEY_PASSWORD=guanmai
```
在版本控制.gitignore中将密码文件或者签名文件忽略掉,在`build.gradle`中读取签名密码文件。  
在android{}函数外部增加设置签名的函数

```
//从../signingfiles/keystore.properties下读取签名文件的位置和密码
//如果没有相关文件或者参数不对将抛出异常
def setSigningProperties(){
    def propFile = file('../signingfiles/keystore.properties')
    if (propFile.canRead()){
        def Properties props = new Properties()
        props.load(new FileInputStream(propFile))
        if (props!=null && props.containsKey('STORE_FILE') && props.containsKey('STORE_PASSWORD') &&props.containsKey('KEY_ALIAS') && props.containsKey('KEY_PASSWORD')) {
            android.signingConfigs.release.storeFile = file(props['STORE_FILE'])
            android.signingConfigs.release.storePassword = props['STORE_PASSWORD']
            android.signingConfigs.release.keyAlias = props['KEY_ALIAS']
            android.signingConfigs.release.keyPassword = props['KEY_PASSWORD']
        } else {
            println 'signing.properties found but some entries are missing'
            throw new Exception("signing.properties found but some entries are missing")
            //android.buildTypes.release.signingConfig = null
        }
    }else {
        println 'signing.properties not found'
        throw new Exception("signing.properties not found:" + propFile.absolutePath)
        //android.buildTypes.release.signingConfig = null
    }
}
```
在`signingconfigs`读取并设置密码和签名文件路径
```
    signingConfigs {
        release {
            setSigningProperties()
        }
    }
```
### 本地配置签名密码
rn官网曾提到的方法http://reactnative.cn/docs/0.42/signed-apk-android.html#content  
签名配置文件放在全局的本地`~/.gradle/gradle.properties`中，签名文件也放到自己的本地`~/.gradle/release-key.keystore`。
```
MYAPP_RELEASE_STORE_FILE=~/.gradle/release-key.keystore
MYAPP_RELEASE_KEY_ALIAS=key-alias
MYAPP_RELEASE_STORE_PASSWORD=pass
MYAPP_RELEASE_KEY_PASSWORD=pass
```
`build.gradle`中配置签名
```
    signingConfigs {
        release {
            storeFile file(MYAPP_RELEASE_STORE_FILE)
			storePassword MYAPP_RELEASE_STORE_PASSWORD
			keyAlias MYAPP_RELEASE_KEY_ALIAS
			keyPassword MYAPP_RELEASE_KEY_PASSWORD
        }
    }
```
这样签名文件与与密码都不在版本控制中，尤其是工作中正式app签名用的都是一个，会很方便。

### 查看签名文件
```
//查看签名文件的属性
keytool -list -keystore 签名文件

//查看 apk 的签名，需要提前解压 apk ，获取 CERT.RSA（位于解压目录下 /META-INF 下）
//以下命令行是在 apk 解压目录下执行
keytool -printcert -file META-INF/CERT.RSA
```
## 版本名命名*VersionName*
*VersionName*是一个字符串，例如 "1.2.0"，这个是给人看的版本名，系统并不关心这个值，但是合理的版本名，对后期的维护和 bug 修复也非常重要。

**参考**
版本号规则
https://developer.android.com/studio/publish/versioning.html
主流app版本号
http://www.ifeegoo.com/recommended-mobile-application-version-name-management-specification.html

版本versionName主要采用形式
v1.0.0.buildnum
1.版本号
`<major>.<minor>.<point> `
* major是主版本号，当项目在进行了重大修改或局部修正累积较多，而导致项目整体发生全局变化时，主版本号加 1;
* minor是次版本号，一般在软件有新功能时增长
* point是修正版本号，一般在软件有主要的问题修复后增长

2.*buidnum*为build次数
可以采用build次数记录自增的方式，参考下面的VersionCode
在文件`gradle.properties`文件中build.num变量

## 如何赋值*versionCode*
版本号，是一个大于 0 的整数，相当于 Build Number，随着版本的更新，这个必须是递增的。大的版本号，覆盖更新小的版本号。那么除了手动增加外，还有其他自增的方案

方案
*  随着build次数自增 http://www.race604.com/android-auto-version/
*  git commit 次数自增 http://www.race604.com/android-auto-version/
*  发布时间转换整数

### build次数自增
1.要实现这个功能，首先得有个变量记录次数，在工程根目录下的`gradle.properties`增加`build.num`属性
```
build.num=1
```
2.需要读取个属性，定义读取这个`build.num`的函数
```
def getBuildNum() {
    def buildFile = file('../gradle.properties')

    if (buildFile.exists()) {
        def Properties buildPop = new Properties()
        buildPop.load(new FileInputStream(buildFile))
        def buildNum = buildPop['build_num'].toInteger()
        println('Current version code is ' + buildNum.toString())
        return buildNum
    } else {
        throw new GradleException("Could not find " + buildFile.absolutePath)
    }
}
```
3.此外我们还得能够赋值这个属性，定义自增函数
```
//加一标志，确保一次编译不管生成几个apk，都只增加一
def add_flage = false
//非'Debug'编译方式，自增
def addBuildNum() {
    def runTasks = gradle.startParameter.taskNames.toString()
    println("runtasks!: " + runTasks)
    if ((runTasks.contains('Debug'))) {
        return
    }

    def File buildFile = file('../gradle.properties')
    if (buildFile.exists()) {
        def Properties versionProps = new Properties()
        versionProps.load(new FileInputStream(buildFile))
        def buildNum = versionProps['build_num'].toInteger()
        buildNum++
        versionProps['build_num'] = buildNum.toString()
        versionProps.store(buildFile.newWriter(), null)
        println('Updated build number to ' + buildNum.toString())
    } else {
        throw new GradleException("Could not find " + buildFile.absolutePath)
    }
}
```
4.在合适条件下build先自增，然后在再给*versionName*和*VesionCode*赋值
```
        //所有编译方式BuildType都会遍历flavors
        productFlavors.all { flavor ->
            //buid自增,一次编译只增一次
            if (!add_flage ){
                addBuildNum()
                add_flage = true;
            }

            //build自增后 重新赋值 versionname;versionCode
            versionName "v1.2.1.${getBuildNum()}"
            versionCode getBuildNum()
        }
```
### git commit 次数自增
参考http://www.race604.com/android-auto-version/

### 发布时间
通过发布时间函数
```
def releaseTime() {
    return new Date().format("yyMMddhhmm", TimeZone.getTimeZone("UTC"))
}
```
将返回的字符串转换为int值，作为versionCode，versionCode为整数有长度限制，忘记了多少，自行查看官网。
## 多应用安装
https://developer.android.com/studio/build/build-variants.html
实现多应用的安装应该有的步骤  
1.首先要让编译有不同产品的编译，定义不同产品风味`productFlavors`  
2.有了不同的产品编译，生成了apk，要定义不同的文件名，生成不同的apk。  
3.生成不同名子的apk，如果*ApplicationId*一样的话，也不能同时安装在一台机器上，要在`productFlavors`定义不同的*ApplicationId*。  
### 产品风味`productFlavors`，生成多个apk

* env 发布测试环境
* release_normal 正式发布正常升级
* company_{公司关键词} 不同公司版本
* 渠道 不同渠道版本

在构建的变体里 可以设置不同的build参数，如applicationId、渠道号、versionName等  
**注意：**android studio 正常运行可以选择构建方式（debug签名文件跟release不一样），可以在左下角的Build Variants 菜单中选择构建方式，注意点看输出文件


### 输出文件
输出的文件名可以根据的自己需要自定义例如  
`${profix}_${projectName}_${variant.versionName}`  
在 applicationVariants.all定义输出文件名  
```
    //输出文件参数设置
    applicationVariants.all { variant ->
        variant.outputs.each { output ->
            def outputFile = output.outputFile
            //只定义release编译方式的apk文件名
            if (outputFile != null && outputFile.name.endsWith('.apk') && 'release'.equals(variant.buildType.name)) {

                def flavor =  variant.productFlavors[0].name;

                //不同的falvor，定义不同的输出的apk文件名前缀
                def profix
                if (flavor.equals("release_normal")){
                    profix = "gm"
                } else if (flavor.equals('env')){
                    profix = "test_gm"
                }else if (flavor.contains("company_")){
                    profix = flavor.replace("company_","");
                }else {
                    profix = "gm_" + flavor
                }

                //定义输出apk文件名
                def fileName = "${profix}_${projectName}_${variant.versionName}.apk".toLowerCase()
                output.outputFile = new File(outputFile.parent, fileName)
            }
        }
    }
```
### 一机多应用安装
根据不同的产品风味`ProductFlavors`，定义不同*applicationId*
```
//以company开头 加入不同公司版
//在这里我可以更改包名 app在手机中可以和其他flavor编译的app共存
company_xnv{
	applicationId "com.versiontest.xnv"
	manifestPlaceholders = [PGYER_APPID_VALUE: "other PGYER_APPID_VALUE"]
}
```

##   不同编译拥有不同资源文件

在 main 的同级目录下创建以*productFlavors*或者*buildTpye*名命名的文件夹，然后创建资源文件，然后打包的时候 gradle 就会自己替换或者合并资源。
例如， App 的默认 string.xml 路径为` main\res\value\string.xml `，那么 相应的构建变体路径 `env\res\value\string.xml` ，打包 *env* 构建变体的时候会自动替换当中的字符串。
在android studio 中右键选择new->android resources directory 弹出对话框 ==source set==选项，选择相应的构建变体，以及相应的资源文件类型，确定就会自动新建相应的目录了。

*main* 目录中：
```
<resources>
    <string name="app_name">GuanmaiDemoGradle</string>
</resources>
```

*env* 目录中
```
<resources>
    <string name="app_name">DemoGradle</string>
</resources>
```
那么用env构建apk，apk的名字就会变成DemoGradle

## 代码中不同编译方式得到不同变量

在实际项目，我们可能需要通过不同的构建变体，得到一些不同的变量，产生不同的业务逻辑，例如想打一个测试环境包，就想得到一个环境变量，赋值不同url前缀。
大约三种方式，  
1.resValue  
2.buildConfigField，  
3.*<meta data>*方式，也是经常看到渠道方式，除了渠道外，很多更改第三方appId等，都可参考。  

###  *“resValue”*在*string.xml*中增加变量

1.在资源文件中新增变量。在你的 gradle 文件 buildTypes 或者 productFlavors 下面，如 release 体内写上类似：  
`resValue "string", "app_name_build", "Test打包Rn"`    
就相当于在资源文件string.xml多了    
`  <string name="app_name_build">Test打包Rn</string>`    
代码中就可以读取string.xml中的`app_name_build`。    

**注意：** 只能在string.xml中新增加字符对，不能改变原有的字符对,或者说不能与原有的字符对重复，会报冲突错误。

### *“buildConfigField”*在*BuildConfig*中新增变量
在BuildConfig中新增变量。在你的 gradle 文件 buildTypes 或者 productFlavors 下赋值新的变量， 例如赋值字符串*ENV*变量：  
```
buildConfigField "String", "ENV", "\"development\""
```
在代码中便可以引用`BuildConfig.ENV`

###  `<meta data>`（渠道）

渠道一般都是通过友盟，以友盟为例来说明渠道方式。  
通过在 AndroidManifest.xml 文件中 application 标签下指定` <mate-data>` 设置占位符来实现动态替换属性值。

```
<meta-data android:name="UMENG_CHANNEL" android:value="${UMENG_CHANNEL}" />
```
在build.gradle中,是渠道的构建变体 ==UMENG_CHANNEL== 为其名字，其他为default
```
if (name.contains("debug") || name.contains("release"))
	flavor.manifestPlaceholders = [UMENG_CHANNEL: "default"]
else
	flavor.manifestPlaceholders = [UMENG_CHANNEL: name]
```

读取*application*中的`<meta data>`
```
  private String getMetaValue(String metaName){
    ApplicationInfo appInfo = null;
    try {
      appInfo = this.getReactApplicationContext().getPackageManager()
              .getApplicationInfo(this.getReactApplicationContext().getPackageName(),
                      PackageManager.GET_META_DATA);
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }
    String msg=appInfo.metaData.getString(metaName);
    return msg;
  }
```
如果渠道很多，就参考下美团和其他github上的多渠道打包  
http://www.jianshu.com/p/76ab2ff11229
