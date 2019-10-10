package backend.search

import backend.logging.Logger
import com.google.inject.Singleton
import javax.inject.Inject
import models.{Album, Artist, Song}

import scala.concurrent.Future

import common.concurrency.Extra

@Singleton
private class SearchState @Inject()(factory: CompositeIndexFactory, logger: Logger) {
  private var index: CompositeIndex = factory.create()
  private val updater = Extra("SearchState", {
    index = factory.create()
    logger info "Search state has been updated"
  })
  def update(): Future[Unit] = updater.!()

  def search(terms: Seq[String]): (Seq[Song], Seq[Album], Seq[Artist]) = index search terms
}
