To open the demo:

1. Compile it: `sbt fastOptJS`
2. Host with some server, e.g. [http-server](https://www.npmjs.com/package/http-server):

    ```bash
    http-server -sp 9999 ./moorka-todomvc/target/scala-2.11/classes/
    ```
3. Open in a browser [http://localhost:9999/index-dev.html](http://localhost:9999/index-dev.html)
