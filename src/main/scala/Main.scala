package main
package parser

import java.io.PrintWriter

import io.circe._
import io.circe.generic.auto._
import io.circe.parser._


object Main {

  case class Actor(name: String, id: Int) {
    def toQuad: String = s"""a$id "type" "actor" .\na$id "name" "$name" .\n"""
  }

  case class Movie(name: String, id: Int, cast: Seq[Actor]) {
    def toQuad: String = s"""m$id "type" "movie" .\nm$id "name" "$name" .\n"""
  }

  def main(args: Array[String]): Unit = {

    val pw = new PrintWriter("quads.nq")

    val input = scala.io.Source.fromFile("src/main/resources/tmdb_5000_credits.csv").getLines().toSeq.tail

    try {
      val maybeMovies = input.map ( i => parseLines(i))

      val movies = maybeMovies.map( mm => mm.right.get )

      movies.foreach(m => pw.write(m.toQuad))

      val r: Seq[Actor] = movies.flatMap(m => m.cast)

      val s = r.toSet

      s.foreach(t => pw.write(t.toQuad))

      movies.foreach { m =>

        val s = m.cast.distinct

        s.foreach { c =>
          pw.write(s"""a${c.id} "acted_in" m${m.id} .\n""")
        }
      }

    } finally {
      pw.close()
    }

  }

  def parseLines(input: String): Either[Error, Movie] = {
    val a = input.split(",")
    val b = a.tail
    val c = b.tail.mkString(",")
    val d = c.split("\\[")
    val cast = s"[${d(1)}".dropRight(1)
    val credit = s"[${d(2)}"

    val as = decode[Seq[Actor]](cast)

    as match {
      case a if a.isLeft =>
        println(input)
        Left(a.left.get)
      case b if b.isRight =>
        Right(Movie(a(1), a(0).toInt, b.right.get))
    }
  }

}