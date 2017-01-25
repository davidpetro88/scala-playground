package com.sundogsoftware.spark


import org.apache.spark.sql.SparkSession
import org.apache.spark.SparkContext._
import org.apache.log4j._


/** Find the movies with the most ratings. 
 *  Example use DataFrame and dataset
 *  */
object PopularMovies2 {
 
  /** Our main function where the action happens */
  def main(args: Array[String]) {

    // Set the log level to only print errors
    Logger.getLogger("org").setLevel(Level.ERROR)
    
    val spark = SparkSession.builder.appName("PopularMovies2").master("local[*]").getOrCreate()
    val lines = spark.read.textFile("../ml-100k/u.data")
    
    // show values
    lines.show();
    
    // Map to (movieID, 1) tuples
    val movies = lines.rdd.map(x => (x.split("\t")(1).toInt, 1))
      
    // Count up all the 1's for each movie
    val movieCounts = movies.reduceByKey( (x, y) => x + y )
        
    // Flip (movieID, count) to (count, movieID)
    val flipped = movieCounts.map( x => (x._2, x._1) )
    
    // Sort
    val sortedMovies = flipped.sortByKey()
    
    // Collect and print results
    val results = sortedMovies.collect()
    
    results.foreach(println)
  }
  
}

