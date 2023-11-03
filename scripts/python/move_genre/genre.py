from __future__ import annotations

from enum import Enum, auto

class Genre(Enum):
  # Rock
  ADULT_CONTEMPORARY = auto()
  AOR = auto()
  ART_ROCK = auto()
  BLUES_ROCK = auto()
  CELTIC = auto()
  CHAMBAROQUE_POP = auto()
  CIRCUS_ROCK = auto()
  CLASSIC_POP = auto()
  CLASSIC_ROCK = auto()
  CLASSICAL_PROG = auto()
  CROSS_OVER_PROG = auto()
  DARK_CABARET = auto()
  DARK_FOLK = auto()
  EMO = auto()
  FOLK_PUNK = auto()
  FOLK_ROCK = auto()
  GLAM_ROCK = auto()
  GOTHIC_ROCK = auto()
  HARD_ROCK_REVIVAL = auto()
  HARD_ROCK = auto()
  HEAVY_PSYCH = auto()
  INDIE = auto()
  INDIE_FOLK = auto()
  INDIE_POP = auto()
  ISRAELI = auto()
  ISRAELI_POP = auto()
  ISRAELI_PROG = auto()
  LATE_PROG = auto()
  MATH_ROCK = auto()
  NEO_PROG = auto()
  NEW_WAVE = auto()
  OCCULT_ROCK_REVIVAL = auto()
  OTHER = auto()
  PIANO_POP = auto()
  PIANO_ROCK = auto()
  POP = auto()
  POP_ROCK = auto()
  POST_HARDCORE = auto()
  POST_PUNK_REVIVAL = auto()
  POST_ROCK = auto()
  PROG_FUSION = auto()
  PUNK = auto()
  ROCK = auto()
  ROCK_N_ROLL = auto()
  ROCK_PROGRESSIVO_ITALIANO = auto()
  SHOEGAZE = auto()
  SOFT_ROCK = auto()
  SYMPHONIC_PROG = auto()
  SYNTH_POP = auto()
  # Metal
  ATMOSPHERIC_BLACK_METAL = auto()
  AVANTGARDE_METAL = auto()
  BLACK_METAL = auto()
  BLACK_N_ROLL = auto()
  BLACKENED_SPEED_METAL = auto()
  BLACKENED_THRASH = auto()
  BLACKGAZE = auto()
  DANCE_METAL = auto()
  DEATH_METAL = auto()
  DJENT = auto()
  DOOM_METAL = auto()
  EPIC_METAL = auto()
  EXTREME_PROGRESSIVE_METAL = auto()
  FLACK_METAL = auto()
  FOLK_METAL = auto()
  FUNERAL_DEATHDOOM = auto()
  GOTHIC_METAL = auto()
  HAIR_METAL = auto()
  HEAVY_METAL = auto()
  JAZZ_METAL = auto()
  MELOBLACK = auto()
  MELODEATH = auto()
  METALCORE = auto()
  NEO_PROG_METAL = auto()
  NWOBHM = auto()
  OTHER_METAL = auto()
  PAGAN_METAL = auto()
  POST_METAL = auto()
  POWER_METAL = auto()
  PROGRESSIVE_BLACK_METAL = auto()
  PROGRESSIVE_DEATH_METAL = auto()
  PROGRESSIVE_DOOM_METAL = auto()
  PROGRESSIVE_METAL = auto()
  SLUDGE = auto()
  SPEED_METAL = auto()
  STONER_METAL = auto()
  SYMPHONIC_BLACK_METAL = auto()
  SYMPHONIC_METAL = auto()
  TECH_THRASH = auto()
  TECHNICAL_DEATH_METAL = auto()
  THRASH = auto()
  THRASH_REVIVAL = auto()

  def name(self) -> str:
    # Rock
    if self == Genre.ADULT_CONTEMPORARY:
      return "Adult Contemporary"
    if self == Genre.AOR:
      return "AOR"
    if self == Genre.ART_ROCK:
      return "Art Rock"
    if self == Genre.BLUES_ROCK:
      return "Blues rock"
    if self == Genre.CELTIC:
      return "Celtic"
    if self == Genre.CHAMBAROQUE_POP:
      return "Chambaroque Pop"
    if self == Genre.CIRCUS_ROCK:
      return "Circus Rock"
    if self == Genre.CLASSIC_POP:
      return "Classic Pop"
    if self == Genre.CLASSIC_ROCK:
      return "Classic Rock"
    if self == Genre.CLASSICAL_PROG:
      return "Classical Prog"
    if self == Genre.CROSS_OVER_PROG:
      return "Cross-over Prog"
    if self == Genre.DARK_CABARET:
      return "Dark Cabaret"
    if self == Genre.DARK_FOLK:
      return "Dark Folk"
    if self == Genre.EMO:
      return "Emo"
    if self == Genre.FOLK_PUNK:
      return "Folk Punk"
    if self == Genre.FOLK_ROCK:
      return "Folk Rock"
    if self == Genre.GLAM_ROCK:
      return "Glam Rock"
    if self == Genre.GOTHIC_ROCK:
      return "Gothic Rock"
    if self == Genre.HARD_ROCK_REVIVAL:
      return "Hard Rock Revival"
    if self == Genre.HARD_ROCK:
      return "Hard-Rock"
    if self == Genre.HEAVY_PSYCH:
      return "Heavy Psych"
    if self == Genre.INDIE:
      return "Indie"
    if self == Genre.INDIE_FOLK:
      return "Indie Folk"
    if self == Genre.INDIE_POP:
      return "Indie Pop"
    if self == Genre.ISRAELI:
      return "Israeli"
    if self == Genre.ISRAELI_POP:
      return "Israeli Pop"
    if self == Genre.ISRAELI_PROG:
      return "Israeli Prog"
    if self == Genre.LATE_PROG:
      return "Late Prog"
    if self == Genre.MATH_ROCK:
      return "Math Rock"
    if self == Genre.NEO_PROG:
      return "Neo-Prog"
    if self == Genre.NEW_WAVE:
      return "New Wave"
    if self == Genre.OCCULT_ROCK_REVIVAL:
      return "Occult Rock Revival"
    if self == Genre.OTHER:
      return "Other"
    if self == Genre.PIANO_POP:
      return "Piano Pop"
    if self == Genre.PIANO_ROCK:
      return "Piano Rock"
    if self == Genre.POP:
      return "Pop"
    if self == Genre.POP_ROCK:
      return "Pop-Rock"
    if self == Genre.POST_HARDCORE:
      return "Post-Hardcore"
    if self == Genre.POST_PUNK_REVIVAL:
      return "Post-punk revival"
    if self == Genre.POST_ROCK:
      return "Post-Rock"
    if self == Genre.PROG_FUSION:
      return "Prog Fusion"
    if self == Genre.PUNK:
      return "Punk"
    if self == Genre.ROCK:
      return "Rock"
    if self == Genre.ROCK_N_ROLL:
      return "Rock n' Roll"
    if self == Genre.ROCK_PROGRESSIVO_ITALIANO:
      return "Rock Progressivo Italiano"
    if self == Genre.SHOEGAZE:
      return "Shoegaze"
    if self == Genre.SOFT_ROCK:
      return "Soft Rock"
    if self == Genre.SYMPHONIC_PROG:
      return "Symphonic Prog"
    if self == Genre.SYNTH_POP:
      return "Synth Pop"
    # Metal
    if self == Genre.ATMOSPHERIC_BLACK_METAL:
      return "Atmospheric Black Metal"
    if self == Genre.AVANTGARDE_METAL:
      return "Avantgarde Metal"
    if self == Genre.BLACK_METAL:
      return "Black Metal"
    if self == Genre.BLACK_N_ROLL:
      return "Black n' Roll"
    if self == Genre.BLACKENED_SPEED_METAL:
      return "Blackened Speed Metal"
    if self == Genre.BLACKENED_THRASH:
      return "Blackened Thrash"
    if self == Genre.BLACKGAZE:
      return "Blackgaze"
    if self == Genre.DANCE_METAL:
      return "Dance Metal"
    if self == Genre.DEATH_METAL:
      return "Death Metal"
    if self == Genre.DJENT:
      return "Djent"
    if self == Genre.DOOM_METAL:
      return "Doom Metal"
    if self == Genre.EPIC_METAL:
      return "Epic Metal"
    if self == Genre.EXTREME_PROGRESSIVE_METAL:
      return "Extreme Progressive Metal"
    if self == Genre.FLACK_METAL:
      return "Flack Metal"
    if self == Genre.FOLK_METAL:
      return "Folk Metal"
    if self == Genre.FUNERAL_DEATHDOOM:
      return "Funeral DeathDoom"
    if self == Genre.GOTHIC_METAL:
      return "Gothic Metal"
    if self == Genre.HAIR_METAL:
      return "Hair Metal"
    if self == Genre.HEAVY_METAL:
      return "Heavy Metal"
    if self == Genre.JAZZ_METAL:
      return "Jazz Metal"
    if self == Genre.MELOBLACK:
      return "Meloblack"
    if self == Genre.MELODEATH:
      return "Melodeath"
    if self == Genre.METALCORE:
      return "Metalcore"
    if self == Genre.NEO_PROG_METAL:
      return "Neo-Prog Metal"
    if self == Genre.NWOBHM:
      return "NWOBHM"
    if self == Genre.OTHER_METAL:
      return "Other Metal"
    if self == Genre.PAGAN_METAL:
      return "Pagan Metal"
    if self == Genre.POST_METAL:
      return "Post Metal"
    if self == Genre.POWER_METAL:
      return "Power Metal"
    if self == Genre.PROGRESSIVE_BLACK_METAL:
      return "Progressive Black Metal"
    if self == Genre.PROGRESSIVE_DEATH_METAL:
      return "Progressive Death Metal"
    if self == Genre.PROGRESSIVE_DOOM_METAL:
      return "Progressive Doom Metal"
    if self == Genre.PROGRESSIVE_METAL:
      return "Progressive Metal"
    if self == Genre.SLUDGE:
      return "Sludge"
    if self == Genre.SPEED_METAL:
      return "Speed Metal"
    if self == Genre.STONER_METAL:
      return "Stoner Metal"
    if self == Genre.SYMPHONIC_BLACK_METAL:
      return "Symphonic Black Metal"
    if self == Genre.SYMPHONIC_METAL:
      return "Symphonic Metal"
    if self == Genre.TECH_THRASH:
      return "Tech Thrash"
    if self == Genre.TECHNICAL_DEATH_METAL:
      return "Technical Death Metal"
    if self == Genre.THRASH:
      return "Thrash"
    if self == Genre.THRASH_REVIVAL:
      return "Thrash Revival"
    raise ValueError(self)
