# zio-couchbase

The library uses ZIO as a dependency since it offers better concurrency using Fiber and much better handling of errors.

##Connecting to a Cluster

```
val connection = connect("localhost", "Administrator", "password")
```

### Opening a bucket

```
for{
  conn <- connect("localhost", "Administrator", "password")
  bkt  <- conn.bucket("test")
} yield bkt
```

### Key value operations

All key value operations are pure, asynchronous, lazy and non-blocking by default.   
It returns a `Task[Fiber[Throwable, A]]`. Fibers are a lightweight mechanism for concurrency.

You can find more information for ZIO#Fiber here: https://zio.dev/docs/datatypes/datatypes_fiber

#### Insert

```
for{
  conn     <- connect("localhost", "Administrator", "password")
  bkt      <- conn.bucket("test")
  coll     <- bkt.defaultCollection
  insFiber <- coll.insert("doc1", JsonObject("hello" -> "world"))
  _        <- insFiber.join
} yield ()
```

#### Get

```
for{
  conn     <- connect("localhost", "Administrator", "password")
  bkt      <- conn.bucket("test")
  coll     <- bkt.defaultCollection
  getFiber <- coll.get("doc1")
  getValue <- getFiber.join
} yield getValue
```

#### Remove

```
for{
  conn     <- connect("localhost", "Administrator", "password")
  bkt      <- conn.bucket("test")
  coll     <- bkt.defaultCollection
  rmvFiber <- coll.remove("doc1")
  _        <- rmvFiber.join
} yield ()
```


