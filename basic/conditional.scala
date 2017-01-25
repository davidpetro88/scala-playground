if(true){
  println("I Will print if True")
}

val x = "Hello"
if(x.endsWith("o")){
  println("The value of x ends with o")
} else {
  println("The value os x does not end with o")
}

val person = "George"
if(person == "Sammy"){
  println("Welcome Sammy")
} else if (person == "George"){
  println("Welcome George")
} else {
  println("whats is your names?")
}

// AND
println("Example AND :")
println((1==2) && (2==2))
println((1==1) && (2==2))

//OR
println("Example OR :")
println((1==2) || (2==2))

//NOT
println("Example NOT :")
println(!(1==1))
