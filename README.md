# Vehicle Data

Challenge done for a company.

## Endpoints

Timestamps are in micros since 1970 01 01 00:00:00 GMT

### GET `/api/operator`

Mandatory query parameters:
* `start`: search timestamp lower bound (inclusive)
* `end` : search timestamp upper bound (inclusive)

### GET `/api/operator/{$operatorId}/vehicle`

Mandatory query parameters:
* `start`: search timestamp lower bound (inclusive)
* `end` : search timestamp upper bound (inclusive)

Optional query parameters:
* `atStop` : boolean to filter only vehicles with "atStop" = (true/ false) 


### GET `/api/vehicle/{$vehicleId}/route`

Mandatory query parameters:
* `start`: search timestamp lower bound (inclusive)
* `end` : search timestamp upper bound (inclusive)

## Running the project

This is an sbt-based project. 

The database used is postgres. You can use docker-compose with the following command to start postgres:

`docker-compose up`

Next you need to run the DataLoader script, which also creates the database table used:

`sbt "runMain com.github.xplosunn.vehicledata.DataLoader path/to/data/file.csv"`

Finally, to run the http server on `localhost:9000` use the following command:

`sbt "runMain com.github.xplosunn.vehicledata.Starter"`

You can confirm the server is running a curl example request: 

`curl -H "Content-Type: application/json" "localhost:9000/api/operator?start=1356998403000000&end=1356998403000000"`

To run the tests use the following command:

`sbt test`
