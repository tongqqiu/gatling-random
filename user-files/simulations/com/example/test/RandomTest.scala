package com.example.test

import java.util.Calendar
import java.text.SimpleDateFormat
import com.excilys.ebi.gatling.core.Predef._
import com.excilys.ebi.gatling.http.Predef._
import com.excilys.ebi.gatling.jdbc.Predef._
import com.excilys.ebi.gatling.http.Headers.Names._
import akka.util.duration._
import bootstrap._
import assertions._
import org.apache.drill.synth.SchemaSampler
import com.google.common.io.Resources
import com.google.common.base.Charsets
import com.fasterxml.jackson.databind.JsonNode

class RandomTest extends Simulation {


  //val hostIP = System.getProperty("hostIP")
  var hostIP = "localhost"
  var url = "http://" + hostIP + ":8090"

  val threads = Integer.getInteger("threads", 1000)
  val rampup = Integer.getInteger("rampup", 10).toLong
  val duration = Integer.getInteger("duration", 120).toLong

  val httpConf = httpConfig
    .baseURL(url)
    .acceptHeader("text/html,application/json,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .connection("keep-alive")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.9; rv:29.0) Gecko/20100101 Firefox/29.0")

  // custom feed generate the usernames randomly.
  val myCustomFeeder = new Feeder[String] {
    // always return true as this feeder can be polled infinitively
    override def hasNext = true

    val s: SchemaSampler = new SchemaSampler(Resources.asCharSource(Resources.getResource("schema003.json"), Charsets.UTF_8).read)

    override def next: Map[String, String] = {
      val record: JsonNode = s.sample
      var name = record.get("name").asText
      var id = record.get("id").asText
      val today = Calendar.getInstance().getTime()
      val formater = new SimpleDateFormat("yyyyMMddHHmmssSSS")
      var todayStr = formater.format(today)
      Map("name" -> name, "id" -> id, "time" -> todayStr)
    }
  }

  val scn = scenario("Write Test")
    .feed(myCustomFeeder)
    .exec(
      http("insert_request")
        .put("/keyvalue/test_collection/${id}") // must be lowercase
        .header("Content-Type", "application/json")
        .body("""{ "username": "${name}" }""").asJSON
        .check(status.in(200 to 210))

    )
  .exec(
      http("get_request")
        .get("/keyvalue/test_collection/${id}") // must be lowercase
        .header("Content-Type", "application/json")
        .check(status.in(200 to 210))

    )
  setUp(
    scn.users(threads).ramp(rampup).delay(1).protocolConfig(httpConf)

  )
}
