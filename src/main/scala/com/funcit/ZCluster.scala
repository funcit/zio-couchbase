package com.funcit

import com.couchbase.client.core.env.Credentials
import com.couchbase.client.scala.Cluster
import com.couchbase.client.scala.analytics.{AnalyticsOptions, AnalyticsResult}
import com.couchbase.client.scala.env.ClusterEnvironment
import com.couchbase.client.scala.query.{QueryOptions, QueryResult}
import com.couchbase.client.scala.search.SearchQuery
import com.couchbase.client.scala.search.result.SearchResult
import com.funcit.ZImplicits._
import zio.Task

class ZCluster private (cluster: Cluster) {

  def bucket(name: String): Task[ZBucket] = {
    val bucket = cluster.async.bucket(name).fromFuture
    new ZBucket(bucket).toZIO
  }

  def query(statement: String, options: QueryOptions = QueryOptions()): Task[QueryResult] = {
    cluster.query(statement, options).fromTry
  }

  def analyticsQuery(statement: String, options: AnalyticsOptions = AnalyticsOptions()): Task[AnalyticsResult] = {
    cluster.analyticsQuery(statement, options).fromTry
  }

  def searchQuery(query: SearchQuery): Task[SearchResult] = {
    cluster.searchQuery(query).fromTry
  }

  def shutdown: Task[Unit] = cluster.shutdown().toZIO
}

object ZCluster {

  def connect(connectionString: String, username: String, password: String): Task[ZCluster] = {
    for{
      c <- Cluster.connect(connectionString, username, password).fromTry
    } yield new ZCluster(c)
  }

  def connect(connectionString: String, credentials: Credentials): Task[ZCluster] = {
    for{
      c <- Cluster.connect(connectionString, credentials).fromTry
    } yield new ZCluster(c)
  }

  def connect(environment: ClusterEnvironment): Task[ZCluster] = {
    for{
      c <- Cluster.connect(environment).fromTry
    } yield new ZCluster(c)
  }
}
