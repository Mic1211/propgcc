cat <<EOF
OUTPUT_FORMAT("${OUTPUT_FORMAT}","${OUTPUT_FORMAT}","${OUTPUT_FORMAT}")
OUTPUT_ARCH(${ARCH})

MEMORY
{
  hub     : ORIGIN = 0, LENGTH = 32K
  cog	  : ORIGIN = 0, LENGTH = 1984 /* 496*4 */
  /* coguser is just an alias for cog, but for overlays */
  coguser : ORIGIN = 0, LENGTH = 1984 /* 496*4 */
  ram     : ORIGIN = 0x20000000, LENGTH = 256M
  rom     : ORIGIN = 0x30000000, LENGTH = 256M
  /* some sections (like the .xmm kernel) are handled specially by the loader */
  dummy   : ORIGIN = 0xe0000000, LENGTH = 1M
}

SECTIONS
{
  /* if we are not relocating (-r flag given) discard the boot section */
  ${RELOCATING- "/DISCARD/ : \{ *(.boot) \}" }

  /* the initial spin boot code, if any */
  ${RELOCATING+ ".boot : \{ *(.boot) \} >hub" }

  ${KERNEL}
  ${XMM_HEADER}

  /* the initial startup code (including constructors) */
  .init ${RELOCATING-0} :
  {
    *(.init*)
  } ${RELOCATING+ ${TEXT_MEMORY}}

  /* Internal text space or external memory.  */
  .text ${RELOCATING-0} :
  {
    *(EXCLUDE_FILE (*.cog) .text*)
    ${RELOCATING+ _etext = . ; }
  } ${RELOCATING+ ${TEXT_MEMORY}}

  /* the final cleanup code (including destructors) */
  .fini ${RELOCATING-0} :
  {
    *(.fini*)
  } ${RELOCATING+ ${TEXT_MEMORY}}

  .hub ${RELOCATING-0} :
  {
    *(.hubstart)
    *(.hubtext*)
    *(.hubdata*)
    *(.hub)
    ${HUB_DATA}
  } ${RELOCATING+ ${HUBTEXT_MEMORY}}
  ${TEXT_DYNAMIC+${DYNAMIC}}

  .ctors ${RELOCATING-0} :
  {
    *(.ctors*)
  } ${RELOCATING+ ${HUBTEXT_MEMORY}}
  .dtors ${RELOCATING-0} :
  {
    *(.dtors*)
  } ${RELOCATING+ ${HUBTEXT_MEMORY}}

  .data	${RELOCATING-0} :
  {
    ${DATA_DATA}
    ${RELOCATING+. = ALIGN(4);}
  } ${RELOCATING+ ${DATA_MEMORY}}

  .bss ${RELOCATING-0} :
  {
    ${RELOCATING+ PROVIDE (__bss_start = .) ; }
    *(.bss)
    *(.bss*)
    *(COMMON)
    ${RELOCATING+ PROVIDE (__bss_end = .) ; }
  } ${RELOCATING+ ${DATA_MEMORY}}

  ${RELOCATING+ ${DATA_HEAP+ ".heap : \{ . += 4; \} ${DATA_MEMORY}"}}
  ${RELOCATING+ ${DATA_HEAP+ ___heap_start = ADDR(.heap) ;}}
  ${RELOCATING+ ${HUB_HEAP+ ".hub_heap : \{ . += 4; \} >hub AT>hub"}}
  ${RELOCATING+ ${HUB_HEAP+ ___hub_heap_start = ADDR(.hub_heap) ;}}

  ${RELOCATING+ ${KERNEL_NAME+ __load_start_kernel = LOADADDR (${KERNEL_NAME}) ;}}
  ${RELOCATING+ ___CTOR_LIST__ = ADDR(.ctors) ;}
  ${RELOCATING+ ___DTOR_LIST__ = ADDR(.dtors) ;}

  .hash        ${RELOCATING-0} : { *(.hash)		}
  .dynsym      ${RELOCATING-0} : { *(.dynsym)		}
  .dynstr      ${RELOCATING-0} : { *(.dynstr)		}
  .gnu.version ${RELOCATING-0} : { *(.gnu.version)	}
  .gnu.version_d ${RELOCATING-0} : { *(.gnu.version_d)	}
  .gnu.version_r ${RELOCATING-0} : { *(.gnu.version_r)	}

  .rel.init    ${RELOCATING-0} : { *(.rel.init)		}
  .rela.init   ${RELOCATING-0} : { *(.rela.init)	}
  .rel.text    ${RELOCATING-0} :
    {
      *(.rel.text)
      ${RELOCATING+*(.rel.text.*)}
      ${RELOCATING+*(.rel.gnu.linkonce.t*)}
    }
  .rela.text   ${RELOCATING-0} :
    {
      *(.rela.text)
      ${RELOCATING+*(.rela.text.*)}
      ${RELOCATING+*(.rela.gnu.linkonce.t*)}
    }
  .rel.fini    ${RELOCATING-0} : { *(.rel.fini)		}
  .rela.fini   ${RELOCATING-0} : { *(.rela.fini)	}
  .rel.rodata  ${RELOCATING-0} :
    {
      *(.rel.rodata)
      ${RELOCATING+*(.rel.rodata.*)}
      ${RELOCATING+*(.rel.gnu.linkonce.r*)}
    }
  .rela.rodata ${RELOCATING-0} :
    {
      *(.rela.rodata)
      ${RELOCATING+*(.rela.rodata.*)}
      ${RELOCATING+*(.rela.gnu.linkonce.r*)}
    }
  .rel.data    ${RELOCATING-0} :
    {
      *(.rel.data)
      ${RELOCATING+*(.rel.data.*)}
      ${RELOCATING+*(.rel.gnu.linkonce.d*)}
    }
  .rela.data   ${RELOCATING-0} :
    {
      *(.rela.data)
      ${RELOCATING+*(.rela.data.*)}
      ${RELOCATING+*(.rela.gnu.linkonce.d*)}
    }
  .rel.ctors   ${RELOCATING-0} : { *(.rel.ctors)	}
  .rela.ctors  ${RELOCATING-0} : { *(.rela.ctors)	}
  .rel.dtors   ${RELOCATING-0} : { *(.rel.dtors)	}
  .rela.dtors  ${RELOCATING-0} : { *(.rela.dtors)	}
  .rel.got     ${RELOCATING-0} : { *(.rel.got)		}
  .rela.got    ${RELOCATING-0} : { *(.rela.got)		}
  .rel.bss     ${RELOCATING-0} : { *(.rel.bss)		}
  .rela.bss    ${RELOCATING-0} : { *(.rela.bss)		}
  .rel.plt     ${RELOCATING-0} : { *(.rel.plt)		}
  .rela.plt    ${RELOCATING-0} : { *(.rela.plt)		}

  /* Stabs debugging sections.  */
  .stab 0 : { *(.stab) }
  .stabstr 0 : { *(.stabstr) }
  .stab.excl 0 : { *(.stab.excl) }
  .stab.exclstr 0 : { *(.stab.exclstr) }
  .stab.index 0 : { *(.stab.index) }
  .stab.indexstr 0 : { *(.stab.indexstr) }
  .comment 0 : { *(.comment) }
 
  /* DWARF debug sections.
     Symbols in the DWARF debugging sections are relative to the beginning
     of the section so we begin them at 0.  */

  /* DWARF 1 */
  .debug          0 : { *(.debug) }
  .line           0 : { *(.line) }

  /* GNU DWARF 1 extensions */
  .debug_srcinfo  0 : { *(.debug_srcinfo .zdebug_srcinfo) }
  .debug_sfnames  0 : { *(.debug_sfnames .zdebug_sfnames) }

  /* DWARF 1.1 and DWARF 2 */
  .debug_aranges  0 : { *(.debug_aranges .zdebug_aranges) }
  .debug_pubnames 0 : { *(.debug_pubnames .zdebug_pubnames) }

  /* DWARF 2 */
  .debug_info     0 : { *(.debug_info${RELOCATING+ .gnu.linkonce.wi.*} .zdebug_info) }
  .debug_abbrev   0 : { *(.debug_abbrev .zdebug_abbrev) }
  .debug_line     0 : { *(.debug_line .zdebug_line) }
  .debug_frame    0 : { *(.debug_frame .zdebug_frame) }
  .debug_str      0 : { *(.debug_str .zdebug_str) }
  .debug_loc      0 : { *(.debug_loc .zdebug_loc) }
  .debug_macinfo  0 : { *(.debug_macinfo .zdebug_macinfo) }

  /* provide some case-sensitive aliases */
  PROVIDE(par = PAR) ;
  PROVIDE(cnt = CNT) ;
  PROVIDE(ina = INA) ;
  PROVIDE(inb = INB) ;
  PROVIDE(outa = OUTA) ;
  PROVIDE(outb = OUTB) ;
  PROVIDE(dira = DIRA) ;
  PROVIDE(dirb = DIRB) ;
  PROVIDE(ctra = CTRA) ;
  PROVIDE(ctrb = CTRB) ;
  PROVIDE(frqa = FRQA) ;
  PROVIDE(frqb = FRQB) ;
  PROVIDE(phsa = PHSA) ;
  PROVIDE(phsb = PHSB) ;
  PROVIDE(vcfg = VCFG) ;
  PROVIDE(vscl = VSCL) ;
  PROVIDE(__hub_end = ADDR(.hub) + SIZEOF(.hub)) ;
  
  /* default initial stack pointer */
  PROVIDE(__stack_end = 0x8000) ;

}
EOF

