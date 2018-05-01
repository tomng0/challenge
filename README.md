# challenge

To run

```bash

mvn install
java -jar ./target/challenge-0.0.1-SNAPSHOT.jar --server.port=12345 # Or whatever port you like

```

### Problem

Create a webservice in Java and deploy it to Heroku (free account)
So it responds to this command

```bash
curl http://yourherokuapp.com/some-app/some-service
```

and returns
```js
{
  "timestamp": "2018-05-01T02:36:11.830-05:00" // the current time in EST
  "calls": 1 // number of calls 
}

```
