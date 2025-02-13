package song_encoder

private sealed trait CodecType
private case object Mp3 extends CodecType
private case object Flac extends CodecType
