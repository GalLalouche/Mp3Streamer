from __future__ import annotations

from enum import Enum, auto


class Genre(Enum):
  # Rock
  ADULT_CONTEMPORARY = auto()
  AOR = auto()
  ART_ROCK = auto()
  BLUES_ROCK = auto()
  BRITPOP = auto()
  BRUTAL_PROG = auto()
  CELTIC = auto()
  CHAMBAROQUE_POP = auto()
  CIRCUS_ROCK = auto()
  CLASSICAL_PROG = auto()
  CLASSIC_POP = auto()
  CLASSIC_ROCK = auto()
  CROSS_OVER_PROG = auto()
  DARK_CABARET = auto()
  DARK_FOLK = auto()
  EMO = auto()
  FOLK_PUNK = auto()
  FOLK_ROCK = auto()
  GLAM_ROCK = auto()
  GOTHIC_ROCK = auto()
  GRUNGE = auto()
  HARD_ROCK = auto()
  HARD_ROCK_REVIVAL = auto()
  HEAVY_PROG = auto()
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
  PROGRESSIVE_FOLK = auto()
  PROGRESSIVE_HARD_ROCK = auto()
  PROG_FUSION = auto()
  PROG_POP = auto()
  PUNK = auto()
  ROCK = auto()
  ROCK_N_ROLL = auto()
  ROCK_PROGRESSIVO_ITALIANO = auto()
  SCREAMO = auto()
  SHOEGAZE = auto()
  SOFT_ROCK = auto()
  SYMPHONIC_PROG = auto()
  SYNTH_POP = auto()
  # Metal
  ATMOSPHERIC_BLACK_METAL = auto()
  AVANTGARDE_METAL = auto()
  BLACKENED_SPEED_METAL = auto()
  BLACKENED_THRASH = auto()
  BLACKGAZE = auto()
  BLACK_METAL = auto()
  BLACK_N_ROLL = auto()
  COSMIC_BLACK_METAL = auto()
  DANCE_METAL = auto()
  DEATH_METAL = auto()
  DJENT = auto()
  DOOM_METAL = auto()
  EPIC_METAL = auto()
  EXTREME_PROGRESSIVE_METAL = auto()
  FLACK_METAL = auto()
  FOLK_METAL = auto()
  FUNERAL_DEATHDOOM = auto()
  GOTHIC_DOOM_METAL = auto()
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
  PROGRESSIVE_POWER_METAL = auto()
  SLUDGE = auto()
  SPEED_METAL = auto()
  STONER_METAL = auto()
  SYMPHONIC_BLACK_METAL = auto()
  SYMPHONIC_DEATH_METAL = auto()
  SYMPHONIC_METAL = auto()
  SYMPHONIC_POWER_METAL = auto()
  TECH_THRASH = auto()
  TECHNICAL_DEATH_METAL = auto()
  THRASH = auto()
  THRASH_REVIVAL = auto()

  def name(self) -> str:
    # Rock
    if self == Genre.AOR:
      return "AOR"
    if self == Genre.CROSS_OVER_PROG:
      return "Cross-over Prog"
    if self == Genre.HARD_ROCK:
      return "Hard-Rock"
    if self == Genre.NEO_PROG:
      return "Neo-Prog"
    if self == Genre.POP_ROCK:
      return "Pop-Rock"
    if self == Genre.POST_HARDCORE:
      return "Post-Hardcore"
    if self == Genre.POST_PUNK_REVIVAL:
      return "Post-punk revival"
    if self == Genre.POST_ROCK:
      return "Post-Rock"
    if self == Genre.ROCK_N_ROLL:
      return "Rock n' Roll"
    # Metal
    if self == Genre.BLACK_N_ROLL:
      return "Black n' Roll"
    if self == Genre.NEO_PROG_METAL:
      return "Neo-Prog Metal"
    if self == Genre.NWOBHM:
      return "NWOBHM"
    return str(self).split('.')[1].replace('_', ' ').title()
