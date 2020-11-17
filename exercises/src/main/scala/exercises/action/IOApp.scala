package exercises.action

import exercises.action.IOExercises.IO

trait IOApp extends App {
  def main(): IO[Unit]

  main().unsafeRun()
}
