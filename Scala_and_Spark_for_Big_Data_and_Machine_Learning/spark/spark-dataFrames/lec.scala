//Start a simple Spark Session
import org.apache.spark.sql.SparkSession
val spark = SparkSession.builder().getOrCreate()

//Create a DataFrame from Spark Session read csv
//Technically known as class Dataset
val df = spark.read.option("header","true").option("inferSchema","true").csv("Sales.csv")

df.printSchema
df.show()

df.groupBy("Company").mean().show()
df.groupBy("Company").count().show()
df.groupBy("Company").max().show()
df.groupBy("Company").min().show()
df.groupBy("Company").sum().show()

df.select(sum("Sales")).show()//all sum
df.select(countDistinct("Sales")).show() //approxCountDistinct
df.select(sumDistinct("Sales")).show()
df.select(variance("Sales")).show()
df.select(stddev("Sales")).show() //avg,max,min,sum,stddev
df.select(collect_set("Sales")).show()

//order Ascending
df.orderBy("Sales").show()
//order Descending
df.orderBy($"Sales".desc).show()
