package backend.lyrics.retrievers

import com.google.inject.BindingAnnotation

import scala.annotation.Annotation
import scala.annotation.meta.param

@param
@BindingAnnotation
private[lyrics] class CompositePassiveParser extends Annotation
