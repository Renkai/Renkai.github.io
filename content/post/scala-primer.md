+++
date = "2015-08-14T17:41:01+08:00"
draft = false
title = "scala 从入门到入门+"

+++

新手向,面向刚从java过渡到scala的同学,目的是写出已已易于维护和阅读的代码.

## 从语句到表达式
> 语句(statement): 一段可执行的代码
> 表达式(expression): 一段可以被求值的代码

在Java中语句和表达式是有区分的,表达式必须在return或者等号右侧,而在scala中,一切都是表达式.

一个例子:
假设我们在公司的内网和外网要从不同的域名访问一样的机器

```
//Java代码
String urlString = null;
String hostName = InetAddress.getLocalHost().getHostName();
if (isInnerHost(hostName)) {
  urlString = "http://inner.host";
} else {
  urlString = "http://outter.host";
}
```
刚转到scala的人很可能这么写

```
var urlString: String = null
var hostName = InetAddress.getLocalHost.getHostName
if (isInnerHost(hostName)) {
  urlString = "http://inner.host"
} else {
  urlString = "http://outter.host"
}
```

我们让它更像scala一点吧

```
val hostName = InetAddress.getLocalHost.getHostName
val urlString = if (isInnerHost(hostName)) {
  "http://inner.host"
} else {
  "http://outter.host"
}
```

> 这样做的好处都有啥?

1. 代码简练,符合直觉
2. urlString 是值而不是变量,有效防止 urlString 在后续的代码中被更改(编译时排错)

很多时候,我们编程时说的安全并不是指怕被黑客破坏掉,而是预防自己因为逗比而让程序崩了.

## 纯函数和非纯函数

纯函数（Pure Function）是这样一种函数——输入输出数据流全是显式（Explicit）的。
显式（Explicit）的意思是，函数与外界交换数据只有一个唯一渠道——参数和返回值；函数从函数外部接受的所有输入信息都通过参数传递到该函数内部；函数输出到函数外部的所有信息都通过返回值传递到该函数外部。

如果一个函数通过隐式（Implicit）方式，从外界获取数据，或者向外部输出数据，那么，该函数就不是纯函数，叫作非纯函数（Impure Function）。
隐式（Implicit）的意思是，函数通过参数和返回值以外的渠道，和外界进行数据交换。比如，读取全局变量，修改全局变量，都叫作以隐式的方式和外界进行数据交换；比如，利用I/O API（输入输出系统函数库）读取配置文件，或者输出到文件，打印到屏幕，都叫做隐式的方式和外界进行数据交换。

```
//一些例子
//纯函数
def add(a:Int,b:Int) = a + b
//非纯函数
var a = 1
def addA(b:Int) = a + b
 
def add(a:Int,b:Int) = {
  println(s"a:$a b:$b")
  a + b
}
def randInt() = Random.nextInt()
```

### 纯函数的好处(来自维基百科)
* 无状态,线程安全,不需要线程同步.
* 纯函数相互调用组装起来的函数,还是纯函数.
* 应用程序或者运行环境(Runtime)可以对纯函数的运算结果进行缓存,运算加快速度.

### 纯函数的好处(来自我的经验)
* 单元测试非常方便!
* 分布式/并发环境下,断点调试的方式无以为继,你需要单元测试.

单元测试什么的,赶紧去 http://www.scalatest.org 试试吧

## 惰性求值/Call by name
>维基百科中惰性求值的解释
>惰性求值（Lazy Evaluation），又称惰性计算、懒惰求值，是一个计算机编程中的一个概念，它的目的是要最小化计算机要做的工作。它有两个相关而又有区别的含意，可以表示为“延迟求值”和“最小化求值”，本条目专注前者，后者请参见最小化计算条目。除可以得到性能的提升外，惰性计算的最重要的好处是它可以构造一个无限的数据类型。
惰性求值的相反是及早求值，这是一个大多数编程语言所拥有的普通计算方式。

### 惰性求值不是新鲜事

```
import scala.io.Source.fromFile
val iter: Iterator[String] =
  fromFile("sampleFile")
    .getLines()
```
文件迭代器就用到了惰性求值.
用户可以完全像操作内存中的数据一样操作文件,然而文件只有一小部分传入了内存中.


### 用lazy关键词指定惰性求值

```
lazy val firstLazy = {
  println("first lazy")
  1
}
lazy val secondLazy = {
  println("second lazy")
  2
} 
def add(a:Int,b:Int) = {
  a+b
}
```

```
//在 scala repl 中的结果
scala> add(secondLazy,firstLazy)
second lazy
first lazy
res0: Int = 3

res0: Int = 3
```

second lazy 先于 first lazy输出了

### Call by value 就是函数参数的惰性求值

```
def firstLazy = {
  println("first lazy")
  1
}
def secondLazy = {
  println("second lazy")
  2
}
def chooseOne(first: Boolean, a: Int, b: Int) = {
  if (first) a else b
}
def chooseOneLazy(first: Boolean, a: => Int, b: => Int) = {
  if (first) a else b
}
```

```
chooseOne(first = true, secondLazy, firstLazy)
//second lazy
//first lazy
//res0: Int = 2
chooseOneLazy(first = true, secondLazy, firstLazy)
//second lazy
//res1: Int = 2
```

对于非纯函数,惰性求值会产生和立即求值产生不一样的结果.

### 一个例子,假设你要建立一个本地缓存

```
//需要查询mysql等,可能来自于一个第三方jar包
def itemIdToShopId: Int => Int  
var cache = Map.empty[Int, Int]
def cachedItemIdToShopId(itemId: Int):Int = {
  cache.get(itemId) match {
    case Some(shopId) => shopId
    case None =>
      val shopId = itemIdToShopId(itemId)
      cache += itemId -> shopId
      shopId
  }
}
```

* 罗辑没什么问题,但测试的时候不方便连mysql怎么办?
* 如果第三方jar包发生了改变,cachedItemIdToShopId也要发生改变.

```
//用你的本地mock来测试程序
def mockItemIdToSHopId: Int => Int
def cachedItemIdToShopId(itemId: Int): Int ={  
  cache.get(itemId) match { 
    case Some(shopId) => shopId
   case None => 
      val shopId = mockItemIdToSHopId(itemId)
      cache += itemId -> shopId
     shopId 
  } 
}   
```

* 在测试的时候用mock,提交前要换成线上的,反复测试的话要反复改动,非常令人沮丧.
* 手工操作容易忙中出错.

```
//将远程请求的结果作为函数的一个参数
def cachedItemIdToShopId(itemId: Int, remoteShopId: Int): Int = {   
  cache.get(itemId) match { 
    case Some(shopId) => shopId 
    case None =>    
     val shopId = remoteShopId  
     cache += itemId -> shopId  
      shopId
  } 
}
//调用这个函数
cachedItemIdToShopId(itemId,itemIdToShopId(itemId))
```
    
* 函数对mysql的依赖没有了
* 不需要在测试和提交时切换代码
* 貌似引入了新问题?

没错,cache根本没有起应有的作用,函数每次执行的时候都调用了itemIdToShopId从远程取数据

```
//改成call by name就没有这个问题啦
def cachedItemIdToShopId(itemId: Int, remoteShopId: =>Int): Int = { 
  cache.get(itemId) match { 
    case Some(shopId) => shopId 
    case None =>    
     val shopId = remoteShopId  
     cache += itemId -> shopId  
      shopId
  } 
}
//调用这个函数
cachedItemIdToShopId(itemId,itemIdToShopId(itemId))
```

* 函数对mysql的依赖没有了
* 不需要在测试和提交时切换代码
* 只在需要的时候查询远程库

## Tuple/case class/模式匹配

### Tuple为编程提供许多便利

* 函数可以通过tuple返回多个值
* tuple可以存储在容器类中,代替java bean
* 可以一次为多个变量赋值

### 使用tuple的例子

```
val (one, two) = (1, 2)     
one //res0: Int = 1 
two //res1: Int = 2         
def sellerAndItemId(orderId: Int): (Int, Int) =
   orderId match {  
    case 0 => (1, 2)    
 }          
val (sellerId, itemId) = sellerAndItemId(0)
sellerId // sellerId: Int = 1
itemId // itemId: Int = 2       
val sellerItem = sellerAndItemId(0)
sellerItem._1 //res4: Int = 1
sellerItem._2 //res5: Int = 2
```

### 用模式匹配增加tuple可读性

```
val sampleList = List((1, 2, 3), (4, 5, 6), (7, 8, 9))
sampleList.map(x => s"${x._1}_${x._2}_${x._3}")
//res0: List[String] = List(1_2_3, 4_5_6, 7_8_9)
sampleList.map {    
  case (orderId, shopId, itemId) =>
    s"${orderId}_${shopId}_$itemId"
}   
//res1: List[String] = List(1_2_3, 4_5_6, 7_8_9)
```
上下两个map做了同样的事情,但下一个map为tuple中的三个值都给了名字,增加了代码的可读性.

### match和java和switch很像,但有区别

1. match是表达式,会返回值
2. match不需要”break”
3. 如果没有任何符合要求的case,match会抛异常,因为是表达式
4. match可以匹配任何东西,switch只能匹配数字或字符串常量

```
//case如果是常量,就在值相等时匹配.
//如果是变量,就匹配任何值.
def describe(x: Any) = x match {
   case 5 => "five" 
   case true => "truth" 
   case "hello" => "hi!"    
   case Nil => "the empty list"
   case somethingElse => "something else " + somethingElse  
}   
```
case class,tuple以及列表都可以在匹配的同时捕获内部的内容.

```
case class Sample(a:String,b:String,c:String,d:String,e:String)
def showContent(x: Any) =
 x match {      
  case Sample(a,b,c,d,e) => 
  s"Sample $a.$b.$c.$d.$e"  
  case (a,b,c,d,e) =>   
  s"tuple $a,$b,$c,$d,$e"   
  case head::second::rest =>    
  s"list head:$head second:$second rest:$rest"
}
```

### Case class
1. 模式匹配过程中其实调用了类的unapply方法
2. Case class 是为模式匹配(以及其他一些方面)提供了特别的便利的类
3. Case class 还是普通的class,但是它自动为你实现了apply,unapply,toString等方法
4. 其实tuple就是泛型的case class

## 用 option 代替 null

### null 的问题
```
Map<String, String> map = ???
String valFor2014 = map.get(“1024”); // null

if (valFor1024 == null)
    abadon();
else doSomething();
```

* null到底代表key找不到还是说1024对应的值就是null?
* 某年某月某日,我把为null则abandon这段代码写了100遍.

### option介绍

* option可以看作是一个容器,容器的size是1或0
* Size为1的时候就是一个`Some[A](x: A)`,size为0的时候就是一个`None`

### 看看scala的map

```
def get(key: A): Option[B]

def getOrElse[B1 >: B](key: A, default: => B1): B1 = get(key) match {
  case Some(v) => v
  case None => default
}
```

* 可以区分Map中到底又没有这个key.
* 我见过许多java项目自己实现了`getOrElse`这个方法并放在一个叫做MapUtils的类里.
* 为什么java经过这么多代演进,Map仍然没有默认包含这个方法,一直想不通.
(写完这段突然发现java8开始包含getOrDefault了)

### 好像没有太大区别?

确实能够区分Map是无值还是值为null了.
但是if(为null) 则 abandon 要写一百遍.
`case Some(v) => v`
`case None => default`
似乎也得写一百遍.

不,不是这样的
不要忘了option是个容器
http://www.scala-lang.org/api/2.11.7/index.html#scala.Option

### 试试容器里的各种方法
```
val a: Option[String] = Some("1024")
val b: Option[String] = None
a.map(_.toInt)
//res0: Option[Int] = Some(1024)
b.map(_.toInt)
//res1: Option[Int] = None,不会甩exception
a.filter(_ == "2048")
//res2: Option[String] = None
b.filter(_ == "2048")
//res3: Option[String] = None
a.getOrElse("2048")
//res4: String = 1024
b.getOrElse("2048")
//res5: String = 2048
a.map(_.toInt)
  .map(_ + 1)
  .map(_ / 5)
  .map(_ / 2 == 0) //res6: Option[Boolean] = Some(false)
//如果是null,恐怕要一连check abandon四遍了
```

### option配合其他容器使用

```
val a: Seq[String] =
  Seq("1", "2", "3", null, "4")
val b: Seq[Option[String]] =
  Seq(Some("1"), Some("2"), Some("3"), None, Some("4"))

a.filter(_ != null).map(_.toInt)
//res0: Seq[Int] = List(1, 2, 3, 4)
//如果你忘了检查,编译器是看不出来的,只能在跑崩的时候抛异常
b.flatMap(_.map(_.toInt))
//res1: Seq[Int] = List(1, 2, 3, 4)
```

* option帮助你把错误扼杀在编译阶段
* flatMap则可以在过滤空值的同时将option恢复为原始数据.

scala原生容器类都对option有良好支持

```
Seq(1,2,3).headOption
//res0: Option[Int] = Some(1)

Seq(1,2,3).find(_ == 5)
//res1: Option[Int] = None

Seq(1,2,3).lastOption
//res2: Option[Int] = Some(3)

Vector(1,2,3).reduceLeft(_ + _)
//res3: Int = 6

Vector(1,2,3).reduceLeftOption(_ + _)
//res4: Option[Int] = Some(6)
//在vector为空的时候也能用

Seq("a", "b", "c", null, "d").map(Option(_))
//res0: Seq[Option[String]] =
// List(Some(a), Some(b), Some(c), None, Some(d))
//原始数据转换成option也很方便

```

## 用Try类保存异常

### 传统异常处理的局限性
```
try {
  1024 / 0
} catch {
  case e: Throwable => e.printStackTrace()
}
```
用try-catch的模式,异常必须在抛出的时候马上处理.
然而在分布式计算中,我们很可能希望将异常集中到一起处理,来避免需要到每台机器上单独看错误日志的窘态.

```
 val seq = Seq(0, 1, 2, 3, 4)
 //seq: Seq[Int] = List(0, 1, 2, 3, 4)

val seqTry = seq.map(x => Try {
  20 / x
})
//seqTry: Seq[scala.util.Try[Int]] = List(Failure(java.lang.ArithmeticException: devide by zero),Success(20), Success(10), Success(6), Success(5))

val succSeq = seqTry.flatMap(_.toOption)
//succSeq: Seq[Int] = List(20, 10, 6, 5) Try可以转换成Option
val succSeq2 = seqTry.collect {
  case Success(x) => x
}
//succSeq2: Seq[Int] = List(20, 10, 6, 5) 和上一个是一样的
val failSeq: Seq[Throwable] = seqTry.collect {
  case Failure(e) => e
}
//failSeq: Seq[Throwable] = List(java.lang.ArithmeticException: devide by zero)
```
Try实例可以序列化,并且在机器间传送.

## 函数是一等公民

### 一个需求
* 假设我们需要检查许多的数字是否符合某一范围
* 范围存储在外部系统中,并且可能随时更改
* 数字范围像这样存储着”>= 3,< 7”

一个java版本

```
List<String> params = new LinkedList<>();
List<Integer> nums = new LinkedList<>();
List<String> marks = new LinkedList<>();

public JavaRangeMatcher(List<String> params) {
    this.params = params;
    for (String param : params) {
        String[] markNum = param.split(" ");
        marks.add(markNum[0]);
        nums.add(Integer.parseInt(markNum[1]));
    }
}

public boolean check(int input) {
    for (int i = 0; i < marks.size(); i++) {
        int num = nums.get(i);
        String mark = marks.get(i);
        if (mark.equals(">") && input <= num) return false;
        if (mark.equals(">=") && input < num) return false;
        if (mark.equals("<") && input >= num) return false;
        if (mark.equals("<=") && input > num) return false;
    }
    return true;
}

List<String> paramsList = new LinkedList<String>() {{
    add(“>= 3”);
    add(“< 7”);
}};
JavaRangeMatcher matcher = new JavaRangeMatcher(paramsList);
int[] inputs = new int[]{1, 3, 5, 7, 9};
for (int input : inputs) {
    System.out.println(matcher.check(input));
}
//给自己有限的时间,想想又没有性能优化的余地
//我们一起来跑跑看
```

一个 scala 版本

```
def exprToInt(expr: String): Int => Boolean = {
  val Array(mark, num, _*) = expr.split(" ")
  val numInt = num.toInt
  mark match {
    case "<" => numInt.>
    case ">" => numInt.<
    case ">=" => numInt.<=
    case "<=" => numInt.>=
  } //返回函数的函数
}

case class RangeMatcher(range: Seq[String]) {
  val rangeFunc: Seq[(Int) => Boolean] = range.map(exprToInt)

  def check(input: Int) = rangeFunc.forall(_(input))
}

def main(args: Array[String]) {
  val requirements = Seq(">= 3", "< 7")
  val rangeMatcher = RangeMatcher(requirements)
  val results = Seq(1, 3, 5, 7, 9).map(rangeMatcher.check)
  println(results.mkString(","))
  //false,true,true,false,false
}

```

## 关于性能
这里有一个[性能测试](http://benchmarksgame.alioth.debian.org/u64/performance.php?test=binarytrees&sort=elapsed)网站

我对于网站测试的结果,我总结的情况就是两点.
1. 排在后面的基本都是动态类型语言,静态类型语言相对容易优化到性能差不多的结果.
2. 同一个语言代码写得好差产生的性能差异,远远比各种语言最好的代码性能差异大.

### 总的来说,程序员越自由,程序性能就越差
不过也有返利,我们之前那个程序就是.

```
//java版本
public static void main(String[] args) {
    List<String> paramsList = new LinkedList<String>() {{
        add(">= 3");
        add("< 7");
    }};
    JavaRangeMatcher matcher = new JavaRangeMatcher(paramsList);
    Random random = new Random();
    long timeBegin = System.currentTimeMillis();
    for (int i = 0; i < 100000000; i++) {
        int input = random.nextInt() % 10;
        matcher.check(input);
    }
    long timeEnd = System.currentTimeMillis();
    System.out.println("java 消耗时间: " + (timeEnd - timeBegin) + " 毫秒");
    //java 消耗时间: 3263 毫秒
}
```

```
//scala版本
def main(args: Array[String]) {
  val requirements = Seq(">= 3", "< 7")
  val rangeMatcher = RangeMatcher(requirements)
  val timeBegin = System.currentTimeMillis()
  0 until 100000000 foreach {
    case _ =>
      rangeMatcher.check(Random.nextInt(10))
  }
  val timeEnd = System.currentTimeMillis()
  println(s"scala 消耗时间 ${timeEnd - timeBegin} 毫秒")
  //scala 消耗时间 2617 毫秒
}
```
想想这是为什么?


## 推荐资源

* 尽情地使用worksheet吧!
* 尽情地用IDE查看标准库的源代码吧!
* 推荐coursera上的课程[progfun](https://www.coursera.org/course/progfun)和[reactive](https://www.coursera.org/course/reactive)
* 尽情地查看文档,推荐软件Dash



