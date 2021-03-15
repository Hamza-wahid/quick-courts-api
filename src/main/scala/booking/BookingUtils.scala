package booking

import java.time.{LocalDateTime, LocalTime}
import java.time.format.DateTimeFormatter

object BookingUtils {

  private val timeFormatter  = DateTimeFormatter.ofPattern("HH:mm")
  private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

  def extractDate(dateTimeString: String): String = {
    LocalDateTime
      .parse(dateTimeString, dateTimeFormatter)
      .toLocalDate
      .toString
  }

  def convertToLocalDate(dateTimeString: String) = LocalDateTime.parse(dateTimeString, dateTimeFormatter)

  def extractTime(dateTimeString: String): String = {
    LocalDateTime
      .parse(dateTimeString, dateTimeFormatter)
      .toLocalTime
      .toString
  }

  def convertToLocalTime(timeString: String): LocalTime = LocalTime.parse(timeString, timeFormatter)


}
