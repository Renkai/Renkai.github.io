package example

import org.scalajs.dom
import org.scalajs.dom.html
import org.scalajs.dom.raw.Element

import scala.scalajs.js.annotation.JSExport

@JSExport
object ScalaJSExample {

  val innerHtml =
    """    <span id="a">笋干太咸@renkai.org</span>:<span id="b">~</span><span id="c">$</span> cat 笋干太咸.txt<br/><br/>
      |
      |    <p>我是笋干太咸,我的ASCII兼容名是Renkai. </p>
      |
      |    <p>我是一名程序员,现在关心的方向是大数据/Scala相关</p>
      |
      |    <p>我的学习笔记在 <a href="http://segmentfault.com/blog/sungantaixian">Segmentfault</a></p>
      |
      |    <p>你还可以在这些地方找到我:</p>
      |
      |    <p>
      |        <a href="https://github.com/Renkai">Github</a>
      |        <a href="http://weibo.com/1761831381">Weibo</a>
      |        <a href="https://twitter.com/renkaige">Twitter</a>
      |    </p>""".stripMargin

  def show(node: Element, innerHtml: String, curr: Int): Unit = {
    if (curr >= innerHtml.length) return
    if (curr > 0 && innerHtml(curr - 1) == '<') {
      val next = innerHtml.indexOf('>', curr)
      show(node, innerHtml, next + 1)
      return
    }
    node.innerHTML = innerHtml.substring(0, curr)
    dom.setTimeout(() => show(node, innerHtml, curr + 1), 20)
  }

  @JSExport
  def main(target: html.Div) = {
    val node = dom.document.createElement("div")
    target.appendChild(node)
    show(node, innerHtml, 0)
  }
}
