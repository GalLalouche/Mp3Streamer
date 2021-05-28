package mains.vimtag.table

import mains.vimtag.InitializerParserTest

// This test doesn't fully match the real case scenario, since it doesn't include vim's table realignment.
class TableInitializerParserTest extends InitializerParserTest(TableInitializer, TableParser)
