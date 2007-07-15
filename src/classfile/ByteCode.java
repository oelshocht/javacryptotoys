/* Copyright (c)2006 Olivier Elshocht
 *
 * ByteCode.java
 *
 * Created on 18 novembre 2006, 21:22
 */

package classfile;

import java.io.*;
import java.util.*;
import security.*;

/**
 *
 * @author Olivier
 */
/* package */ class ByteCode
{
    // ==================== OPCODE VALUES ====================

    public static final int NOP             =  0; // (0x00) nop
    public static final int ACONST_NULL     =  1; // (0x01) aconst_null
    public static final int ICONST_M1       =  2; // (0x02) iconst_m1
    public static final int ICONST_0        =  3; // (0x03) iconst_0
    public static final int ICONST_1        =  4; // (0x04) iconst_1
    public static final int ICONST_2        =  5; // (0x05) iconst_2
    public static final int ICONST_3        =  6; // (0x06) iconst_3
    public static final int ICONST_4        =  7; // (0x07) iconst_4
    public static final int ICONST_5        =  8; // (0x08) iconst_5
    public static final int LCONST_0        =  9; // (0x09) lconst_0
    public static final int LCONST_1        = 10; // (0x0a) lconst_1
    public static final int FCONST_0        = 11; // (0x0b) fconst_0
    public static final int FCONST_1        = 12; // (0x0c) fconst_1
    public static final int FCONST_2        = 13; // (0x0d) fconst_2
    public static final int DONCST_0        = 14; // (0x0e) dconst_0
    public static final int DCONST_1        = 15; // (0x0f) dconst_1
    public static final int BIPUSH          = 16; // (0x10) bipush
    public static final int SIPUSH          = 17; // (0x11) sipush
    public static final int LDC             = 18; // (0x12) ldc
    public static final int LDC_W           = 19; // (0x13) ldc_w
    public static final int LDC2_W          = 20; // (0x14) ldc2_w
    public static final int ILOAD           = 21; // (0x15) iload
    public static final int LLOAD           = 22; // (0x16) lload
    public static final int FLOAD           = 23; // (0x17) fload
    public static final int DLOAD           = 24; // (0x18) dload
    public static final int ALOAD           = 25; // (0x19) aload
    public static final int ILOAD_0         = 26; // (0x1a) iload_0
    public static final int ILOAD_1         = 27; // (0x1b) iload_1
    public static final int ILOAD_2         = 28; // (0x1c) iload_2
    public static final int ILOAD_3         = 29; // (0x1d) iload_3
    public static final int LLOAD_0         = 30; // (0x1e) lload_0
    public static final int LLOAD_1         = 31; // (0x1f) lload_1
    public static final int LLOAD_2         = 32; // (0x20) lload_2
    public static final int LLOAD_3         = 33; // (0x21) lload_3
    public static final int FLOAD_0         = 34; // (0x22) fload_0
    public static final int FLOAD_1         = 35; // (0x23) fload_1
    public static final int FLOAD_2         = 36; // (0x24) fload_2
    public static final int FLOAD_3         = 37; // (0x25) fload_3
    public static final int DLOAD_0         = 38; // (0x26) dload_0
    public static final int DLOAD_1         = 39; // (0x27) dload_1
    public static final int DLOAD_2         = 40; // (0x28) dload_2
    public static final int DLOAD_3         = 41; // (0x29) dload_3
    public static final int ALOAD_0         = 42; // (0x2a) aload_0
    public static final int ALOAD_1         = 43; // (0x2b) aload_1
    public static final int ALOAD_2         = 44; // (0x2c) aload_2
    public static final int ALOAD_3         = 45; // (0x2d) aload_3
    public static final int IALOAD          = 46; // (0x2e) iaload
    public static final int LALOAD          = 47; // (0x2f) laload
    public static final int FALOAD          = 48; // (0x30) faload
    public static final int DALOAD          = 49; // (0x31) daload
    public static final int AALOAD          = 50; // (0x32) aaload
    public static final int BALOAD          = 51; // (0x33) baload
    public static final int CALOAD          = 52; // (0x34) caload
    public static final int SALOAD          = 53; // (0x35) saload
    public static final int ISTORE          = 54; // (0x36) istore
    public static final int LSTORE          = 55; // (0x37) lstore
    public static final int FSTORE          = 56; // (0x38) fstore
    public static final int DSTORE          = 57; // (0x39) dstore
    public static final int ASTORE          = 58; // (0x3a) astore
    public static final int ISTORE_0        = 59; // (0x3b) istore_0
    public static final int ISTORE_1        = 60; // (0x3c) istore_1
    public static final int ISTORE_2        = 61; // (0x3d) istore_2
    public static final int ISTORE_3        = 62; // (0x3e) istore_3
    public static final int LSTORE_0        = 63; // (0x3f) lstore_0
    public static final int LSTORE_1        = 64; // (0x40) lstore_1
    public static final int LSTORE_2        = 65; // (0x41) lstore_2
    public static final int LSTORE_3        = 66; // (0x42) lstore_3
    public static final int FSTORE_0        = 67; // (0x43) fstore_0
    public static final int FSTORE_1        = 68; // (0x44) fstore_1
    public static final int FSTORE_2        = 69; // (0x45) fstore_2
    public static final int FSTORE_3        = 70; // (0x46) fstore_3
    public static final int DSTORE_0        = 71; // (0x47) dstore_0
    public static final int DSTORE_1        = 72; // (0x48) dstore_1
    public static final int DSTORE_2        = 73; // (0x49) dstore_2
    public static final int DSTORE_3        = 74; // (0x4a) dstore_3
    public static final int ASTORE_0        = 75; // (0x4b) astore_0
    public static final int ASTORE_1        = 76; // (0x4c) astore_1
    public static final int ASTORE_2        = 77; // (0x4d) astore_2
    public static final int ASTORE_3        = 78; // (0x4e) astore_3
    public static final int IASTORE         = 79; // (0x4f) iastore
    public static final int LASTORE         = 80; // (0x50) lastore
    public static final int FASTORE         = 81; // (0x51) fastore
    public static final int DASTORE         =  82; // (0x52) dastore
    public static final int AASTORE         =  83; // (0x53) aastore
    public static final int BASTORE         =  84; // (0x54) bastore
    public static final int CATORE          =  85; // (0x55) castore
    public static final int SASTORE         =  86; // (0x56) sastore
    public static final int POP             =  87; // (0x57) pop
    public static final int POP2            =  88; // (0x58) pop2
    public static final int DUP             =  89; // (0x59) dup
    public static final int DUP_X1          =  90; // (0x5a) dup_x1
    public static final int DUP_X2          =  91; // (0x5b) dup_x2
    public static final int DUP2            =  92; // (0x5c) dup2
    public static final int DUP2_X1         =  93; // (0x5d) dup2_x1
    public static final int DUP2_X2         =  94; // (0x5e) dup2_x2
    public static final int SWAP            =  95; // (0x5f) swap
    public static final int IADD            =  96; // (0x60) iadd
    public static final int LADD            =  97; // (0x61) ladd
    public static final int FADD            =  98; // (0x62) fadd
    public static final int DADD            =  99; // (0x63) dadd
    public static final int ISUB            = 100; // (0x64) isub
    public static final int LSUB            = 101; // (0x65) lsub
    public static final int FSUB            = 102; // (0x66) fsub
    public static final int DSUB            = 103; // (0x67) dsub
    public static final int IMUL            = 104; // (0x68) imul
    public static final int LMUL            = 105; // (0x69) lmul
    public static final int FMUL            = 106; // (0x6a) fmul
    public static final int DMUL            = 107; // (0x6b) dmul
    public static final int IDIV            = 108; // (0x6c) idiv
    public static final int LDIV            = 109; // (0x6d) ldiv
    public static final int FDIV            = 110; // (0x6e) fdiv
    public static final int DDIV            = 111; // (0x6f) ddiv
    public static final int IREM            = 112; // (0x70) irem
    public static final int LREM            = 113; // (0x71) lrem
    public static final int FREM            = 114; // (0x72) frem
    public static final int DREM            = 115; // (0x73) drem
    public static final int INEG            = 116; // (0x74).......ineg
    public static final int LNEG            = 117; // (0x75) lneg
    public static final int FNEG            = 118; // (0x76) fneg
    public static final int DNEG            = 119; // (0x77) dneg
    public static final int ISHL            = 120; // (0x78) ishl
    public static final int LSHL            = 121; // (0x79) lshl
    public static final int ISHR            = 122; // (0x7a) ishr
    public static final int LSHR            = 123; // (0x7b) lshr
    public static final int IUSHR           = 124; // (0x7c) iushr
    public static final int LUSHR           = 125; // (0x7d) lushr
    public static final int IAND            = 126; // (0x7e) iand
    public static final int LAND            = 127; // (0x7f) land
    public static final int IOR             = 128; // (0x80) ior
    public static final int LOR             = 129; // (0x81) lor
    public static final int IXOR            = 130; // (0x82) ixor
    public static final int LXOR            = 131; // (0x83) lxor
    public static final int IINC            = 132; // (0x84) iinc
    public static final int I2L             = 133; // (0x85) i2l
    public static final int I2F             = 134; // (0x86) i2f
    public static final int I2D             = 135; // (0x87) i2d
    public static final int L2I             = 136; // (0x88) l2i
    public static final int L2F             = 137; // (0x89) l2f
    public static final int L2D             = 138; // (0x8a) l2d
    public static final int F2I             = 139; // (0x8b) f2i
    public static final int F2L             = 140; // (0x8c) f2l
    public static final int F2D             = 141; // (0x8d) f2d
    public static final int D2I             = 142; // (0x8e) d2i
    public static final int D2L             = 143; // (0x8f) d2l
    public static final int D2F             = 144; // (0x90) d2f
    public static final int I2B             = 145; // (0x91) i2b
    public static final int I2C             = 146; // (0x92) i2c
    public static final int I2S             = 147; // (0x93) i2s
    public static final int LCMP            = 148; // (0x94) lcmp
    public static final int FCMPL           = 149; // (0x95) fcmpl
    public static final int FCMPG           = 150; // (0x96) fcmpg
    public static final int DCMPL           = 151; // (0x97) dcmpl
    public static final int DCMPG           = 152; // (0x98) dcmpg
    public static final int IFEQ            = 153; // (0x99) ifeq
    public static final int IFNE            = 154; // (0x9a) ifne
    public static final int IFLT            = 155; // (0x9b) iflt
    public static final int IFGE            = 156; // (0x9c) ifge
    public static final int IFGT            = 157; // (0x9d) ifgt
    public static final int IFLE            = 158; // (0x9e) ifle
    public static final int IF_ICMPEQ       = 159; // (0x9f) if_icmpeq
    public static final int IF_ICMPNE       = 160; // (0xa0) if_icmpne
    public static final int IF_ICMPLT       = 161; // (0xa1) if_icmplt
    public static final int IF_ICMPGE       = 162; // (0xa2) if_icmpge
    public static final int IF_ICMPGT       = 163; // (0xa3) if_icmpgt
    public static final int IF_ICMPLE       = 164; // (0xa4) if_icmple
    public static final int IF_ACMPEQ       = 165; // (0xa5) if_acmpeq
    public static final int IF_ACMPNE       = 166; // (0xa6) if_acmpne
    public static final int GOTO            = 167; // (0xa7) goto
    public static final int JSR             = 168; // (0xa8) jsr
    public static final int RET             = 169; // (0xa9) ret
    public static final int TABLESWITCH     = 170; // (0xaa) tableswitch
    public static final int LOOKUPSWITCH    = 171; // (0xab) lookupswitch
    public static final int IRETURN         = 172; // (0xac) ireturn
    public static final int LRETURN         = 173; // (0xad) lreturn
    public static final int FRETURN         = 174; // (0xae) freturn
    public static final int DRETURN         = 175; // (0xaf) dreturn
    public static final int ARETURN         = 176; // (0xb0) areturn
    public static final int RETURN          = 177; // (0xb1) return
    public static final int GETSTATIC       = 178; // (0xb2) getstatic
    public static final int PUTSTATIC       = 179; // (0xb3) putstatic
    public static final int GETFIELD        = 180; // (0xb4) getfield
    public static final int PUTFIELD        = 181; // (0xb5) putfield
    public static final int INVOKEVIRTUAL   = 182; // (0xb6) invokevirtual
    public static final int INVOKESPECIAL   = 183; // (0xb7) invokespecial
    public static final int INVOKESTATIC    = 184; // (0xb8) invokestatic
    public static final int INVOKEINTERFACE = 185; // (0xb9) invokeinterface
    public static final int XXXUNUSEDXXX    = 186; // (0xba) xxxunusedxxx
    public static final int NEW             = 187; // (0xbb) new
    public static final int NEWARRAY        = 188; // (0xbc) newarray
    public static final int ANEWARRAY       = 189; // (0xbd) anewarray
    public static final int ARRAYLENGTH     = 190; // (0xbe) arraylength
    public static final int ATHROW          = 191; // (0xbf) athrow
    public static final int CHECKCAST       = 192; // (0xc0) checkcast
    public static final int INSTANCEOF      = 193; // (0xc1) instanceof
    public static final int MONITORENTER    = 194; // (0xc2) monitorenter
    public static final int MONITOREXIT     = 195; // (0xc3) monitorexit
    public static final int WIDE            = 196; // (0xc4) wide
    public static final int MULTIANEWARRAY  = 197; // (0xc5) multianewarray
    public static final int IFNULL          = 198; // (0xc6) ifnull
    public static final int IFNONNULL       = 199; // (0xc7) ifnonnull
    public static final int GOTO_W          = 200; // (0xc8) goto_w
    public static final int JSR_W           = 201; // (0xc9) jsr_w
    // Reserved opcodes
    public static final int BREAKPOINT      = 202; // (0xca) breakpoint
    public static final int IMPDEP1         = 254; // (0xfe) impdep1
    public static final int IMPDEP2         = 255; // (0xff) impdep2

    public static final int LAST_OPCODE     = 201;


    // ==================== OPCODE MNEMONICS ====================

    private static final String[] OPCODE_MNEMONIC =
    {
        "nop",             // (0x00)
        "aconst_null",     // (0x01)
        "iconst_m1",       // (0x02)
        "iconst_0",        // (0x03)
        "iconst_1",        // (0x04)
        "iconst_2",        // (0x05)
        "iconst_3",        // (0x06)
        "iconst_4",        // (0x07)
        "iconst_5",        // (0x08)
        "lconst_0",        // (0x09)
        "lconst_1",        // (0x0a)
        "fconst_0",        // (0x0b)
        "fconst_1",        // (0x0c)
        "fconst_2",        // (0x0d)
        "dconst_0",        // (0x0e)
        "dconst_1",        // (0x0f)
        "bipush",          // (0x10)
        "sipush",          // (0x11)
        "ldc",             // (0x12)
        "ldc_w",           // (0x13)
        "ldc2_w",          // (0x14)
        "iload",           // (0x15)
        "lload",           // (0x16)
        "fload",           // (0x17)
        "dload",           // (0x18)
        "aload",           // (0x19)
        "iload_0",         // (0x1a)
        "iload_1",         // (0x1b)
        "iload_2",         // (0x1c)
        "iload_3",         // (0x1d)
        "lload_0",         // (0x1e)
        "lload_1",         // (0x1f)
        "lload_2",         // (0x20)
        "lload_3",         // (0x21)
        "fload_0",         // (0x22)
        "fload_1",         // (0x23)
        "fload_2",         // (0x24)
        "fload_3",         // (0x25)
        "dload_0",         // (0x26)
        "dload_1",         // (0x27)
        "dload_2",         // (0x28)
        "dload_3",         // (0x29)
        "aload_0",         // (0x2a)
        "aload_1",         // (0x2b)
        "aload_2",         // (0x2c)
        "aload_3",         // (0x2d)
        "iaload",          // (0x2e)
        "laload",          // (0x2f)
        "faload",          // (0x30)
        "daload",          // (0x31)
        "aaload",          // (0x32)
        "baload",          // (0x33)
        "caload",          // (0x34)
        "saload",          // (0x35)
        "istore",          // (0x36)
        "lstore",          // (0x37)
        "fstore",          // (0x38)
        "dstore",          // (0x39)
        "astore",          // (0x3a)
        "istore_0",        // (0x3b)
        "istore_1",        // (0x3c)
        "istore_2",        // (0x3d)
        "istore_3",        // (0x3e)
        "lstore_0",        // (0x3f)
        "lstore_1",        // (0x40)
        "lstore_2",        // (0x41)
        "lstore_3",        // (0x42)
        "fstore_0",        // (0x43)
        "fstore_1",        // (0x44)
        "fstore_2",        // (0x45)
        "fstore_3",        // (0x46)
        "dstore_0",        // (0x47)
        "dstore_1",        // (0x48)
        "dstore_2",        // (0x49)
        "dstore_3",        // (0x4a)
        "astore_0",        // (0x4b)
        "astore_1",        // (0x4c)
        "astore_2",        // (0x4d)
        "astore_3",        // (0x4e)
        "iastore",         // (0x4f)
        "lastore",         // (0x50)
        "fastore",         // (0x51)
        "dastore",         // (0x52)
        "aastore",         // (0x53)
        "bastore",         // (0x54)
        "castore",         // (0x55)
        "sastore",         // (0x56)
        "pop",             // (0x57)
        "pop2",            // (0x58)
        "dup",             // (0x59)
        "dup_x1",          // (0x5a)
        "dup_x2",          // (0x5b)
        "dup2",            // (0x5c)
        "dup2_x1",         // (0x5d)
        "dup2_x2",         // (0x5e)
        "swap",            // (0x5f)
        "iadd",            // (0x60)
        "ladd",            // (0x61)
        "fadd",            // (0x62)
        "dadd",            // (0x63)
        "isub",            // (0x64)
        "lsub",            // (0x65)
        "fsub",            // (0x66)
        "dsub",            // (0x67)
        "imul",            // (0x68)
        "lmul",            // (0x69)
        "fmul",            // (0x6a)
        "dmul",            // (0x6b)
        "idiv",            // (0x6c)
        "ldiv",            // (0x6d)
        "fdiv",            // (0x6e)
        "ddiv",            // (0x6f)
        "irem",            // (0x70)
        "lrem",            // (0x71)
        "frem",            // (0x72)
        "drem",            // (0x73)
        "ineg",            // (0x74)
        "lneg",            // (0x75)
        "fneg",            // (0x76)
        "dneg",            // (0x77)
        "ishl",            // (0x78)
        "lshl",            // (0x79)
        "ishr",            // (0x7a)
        "lshr",            // (0x7b)
        "iushr",           // (0x7c)
        "lushr",           // (0x7d)
        "iand",            // (0x7e)
        "land",            // (0x7f)
        "ior",             // (0x80)
        "lor",             // (0x81)
        "ixor",            // (0x82)
        "lxor",            // (0x83)
        "iinc",            // (0x84)
        "i2l",             // (0x85)
        "i2f",             // (0x86)
        "i2d",             // (0x87)
        "l2i",             // (0x88)
        "l2f",             // (0x89)
        "l2d",             // (0x8a)
        "f2i",             // (0x8b)
        "f2l",             // (0x8c)
        "f2d",             // (0x8d)
        "d2i",             // (0x8e)
        "d2l",             // (0x8f)
        "d2f",             // (0x90)
        "i2b",             // (0x91)
        "i2c",             // (0x92)
        "i2s",             // (0x93)
        "lcmp",            // (0x94)
        "fcmpl",           // (0x95)
        "fcmpg",           // (0x96)
        "dcmpl",           // (0x97)
        "dcmpg",           // (0x98)
        "ifeq",            // (0x99)
        "ifne",            // (0x9a)
        "iflt",            // (0x9b)
        "ifge",            // (0x9c)
        "ifgt",            // (0x9d)
        "ifle",            // (0x9e)
        "if_icmpeq",       // (0x9f)
        "if_icmpne",       // (0xa0)
        "if_icmplt",       // (0xa1)
        "if_icmpge",       // (0xa2)
        "if_icmpgt",       // (0xa3)
        "if_icmple",       // (0xa4)
        "if_acmpeq",       // (0xa5)
        "if_acmpne",       // (0xa6)
        "goto",            // (0xa7)
        "jsr",             // (0xa8)
        "ret",             // (0xa9)
        "tableswitch",     // (0xaa)
        "lookupswitch",    // (0xab)
        "ireturn",         // (0xac)
        "lreturn",         // (0xad)
        "freturn",         // (0xae)
        "dreturn",         // (0xaf)
        "areturn",         // (0xb0)
        "return",          // (0xb1)
        "getstatic",       // (0xb2)
        "putstatic",       // (0xb3)
        "getfield",        // (0xb4)
        "putfield",        // (0xb5)
        "invokevirtual",   // (0xb6)
        "invokespecial",   // (0xb7)
        "invokestatic",    // (0xb8)
        "invokeinterface", // (0xb9)
        "xxxunusedxxx",    // (0xba)
        "new",             // (0xbb)
        "newarray",        // (0xbc)
        "anewarray",       // (0xbd)
        "arraylength",     // (0xbe)
        "athrow",          // (0xbf)
        "checkcast",       // (0xc0)
        "instanceof",      // (0xc1)
        "monitorenter",    // (0xc2)
        "monitorexit",     // (0xc3)
        "wide",            // (0xc4)
        "multianewarray",  // (0xc5)
        "ifnull",          // (0xc6)
        "ifnonnull",       // (0xc7)
        "goto_w",          // (0xc8)
        "jsr_w",           // (0xc9)
        null
    };


    // ==================== INSTRUCTION SIZES ====================

    private static int[] INSTRUCTION_SIZE =
    {
            1, // (0x00) nop
            1, // (0x01) aconst_null
            1, // (0x02) iconst_m1
            1, // (0x03) iconst_0
            1, // (0x04) iconst_1
            1, // (0x05) iconst_2
            1, // (0x06) iconst_3
            1, // (0x07) iconst_4
            1, // (0x08) iconst_5
            1, // (0x09) lconst_0
            1, // (0x0a) lconst_1
            1, // (0x0b) fconst_0
            1, // (0x0c) fconst_1
            1, // (0x0d) fconst_2
            1, // (0x0e) dconst_0
            1, // (0x0f) dconst_1
                2, // (0x10) bipush
                    3, // (0x11) sipush
                2, // (0x12) ldc
                    3, // (0x13) ldc_w
                    3, // (0x14) ldc2_w
                2, // (0x15) iload
                2, // (0x16) lload
                2, // (0x17) fload
                2, // (0x18) dload
                2, // (0x19) aload
            1, // (0x1a) iload_0
            1, // (0x1b) iload_1
            1, // (0x1c) iload_2
            1, // (0x1d) iload_3
            1, // (0x1e) lload_0
            1, // (0x1f) lload_1
            1, // (0x20) lload_2
            1, // (0x21) lload_3
            1, // (0x22) fload_0
            1, // (0x23) fload_1
            1, // (0x24) fload_2
            1, // (0x25) fload_3
            1, // (0x26) dload_0
            1, // (0x27) dload_1
            1, // (0x28) dload_2
            1, // (0x29) dload_3
            1, // (0x2a) aload_0
            1, // (0x2b) aload_1
            1, // (0x2c) aload_2
            1, // (0x2d) aload_3
            1, // (0x2e) iaload
            1, // (0x2f) laload
            1, // (0x30) faload
            1, // (0x31) daload
            1, // (0x32) aaload
            1, // (0x33) baload
            1, // (0x34) caload
            1, // (0x35) saload
                2, // (0x36) istore
                2, // (0x37) lstore
                2, // (0x38) fstore
                2, // (0x39) dstore
                2, // (0x3a) astore
            1, // (0x3b) istore_0
            1, // (0x3c) istore_1
            1, // (0x3d) istore_2
            1, // (0x3e) istore_3
            1, // (0x3f) lstore_0
            1, // (0x40) lstore_1
            1, // (0x41) lstore_2
            1, // (0x42) lstore_3
            1, // (0x43) fstore_0
            1, // (0x44) fstore_1
            1, // (0x45) fstore_2
            1, // (0x46) fstore_3
            1, // (0x47) dstore_0
            1, // (0x48) dstore_1
            1, // (0x49) dstore_2
            1, // (0x4a) dstore_3
            1, // (0x4b) astore_0
            1, // (0x4c) astore_1
            1, // (0x4d) astore_2
            1, // (0x4e) astore_3
            1, // (0x4f) iastore
            1, // (0x50) lastore
            1, // (0x51) fastore
            1, // (0x52) dastore
            1, // (0x53) aastore
            1, // (0x54) bastore
            1, // (0x55) castore
            1, // (0x56) sastore
            1, // (0x57) pop
            1, // (0x58) pop2
            1, // (0x59) dup
            1, // (0x5a) dup_x1
            1, // (0x5b) dup_x2
            1, // (0x5c) dup2
            1, // (0x5d) dup2_x1
            1, // (0x5e) dup2_x2
            1, // (0x5f) swap
            1, // (0x60) iadd
            1, // (0x61) ladd
            1, // (0x62) fadd
            1, // (0x63) dadd
            1, // (0x64) isub
            1, // (0x65) lsub
            1, // (0x66) fsub
            1, // (0x67) dsub
            1, // (0x68) imul
            1, // (0x69) lmul
            1, // (0x6a) fmul
            1, // (0x6b) dmul
            1, // (0x6c) idiv
            1, // (0x6d) ldiv
            1, // (0x6e) fdiv
            1, // (0x6f) ddiv
            1, // (0x70) irem
            1, // (0x71) lrem
            1, // (0x72) frem
            1, // (0x73) drem
            1, // (0x74) ineg
            1, // (0x75) lneg
            1, // (0x76) fneg
            1, // (0x77) dneg
            1, // (0x78) ishl
            1, // (0x79) lshl
            1, // (0x7a) ishr
            1, // (0x7b) lshr
            1, // (0x7c) iushr
            1, // (0x7d) lushr
            1, // (0x7e) iand
            1, // (0x7f) land
            1, // (0x80) ior
            1, // (0x81) lor
            1, // (0x82) ixor
            1, // (0x83) lxor
                    3, // (0x84) iinc
            1, // (0x85) i2l
            1, // (0x86) i2f
            1, // (0x87) i2d
            1, // (0x88) l2i
            1, // (0x89) l2f
            1, // (0x8a) l2d
            1, // (0x8b) f2i
            1, // (0x8c) f2l
            1, // (0x8d) f2d
            1, // (0x8e) d2i
            1, // (0x8f) d2l
            1, // (0x90) d2f
            1, // (0x91) i2b
            1, // (0x92) i2c
            1, // (0x93) i2s
            1, // (0x94) lcmp
            1, // (0x95) fcmpl
            1, // (0x96) fcmpg
            1, // (0x97) dcmpl
            1, // (0x98) dcmpg
                    3, // (0x99) ifeq
                    3, // (0x9a) ifne
                    3, // (0x9b) iflt
                    3, // (0x9c) ifge
                    3, // (0x9d) ifgt
                    3, // (0x9e) ifle
                    3, // (0x9f) if_icmpeq
                    3, // (0xa0) if_icmpne
                    3, // (0xa1) if_icmplt
                    3, // (0xa2) if_icmpge
                    3, // (0xa3) if_icmpgt
                    3, // (0xa4) if_icmple
                    3, // (0xa5) if_acmpeq
                    3, // (0xa6) if_acmpne
                    3, // (0xa7) goto
                    3, // (0xa8) jsr
                2, // (0xa9) ret
        0, // (0xaa) tableswitch
        0, // (0xab) lookupswitch
            1, // (0xac) ireturn
            1, // (0xad) lreturn
            1, // (0xae) freturn
            1, // (0xaf) dreturn
            1, // (0xb0) areturn
            1, // (0xb1) return
                    3, // (0xb2) getstatic
                    3, // (0xb3) putstatic
                    3, // (0xb4) getfield
                    3, // (0xb5) putfield
                    3, // (0xb6) invokevirtual
                    3, // (0xb7) invokespecial
                    3, // (0xb8) invokestatic
                            5, // (0xb9) invokeinterface
        0, // (0xba) xxxunusedxxx
                    3, // (0xbb) new
                2, // (0xbc) newarray
                    3, // (0xbd) anewarray
            1, // (0xbe) arraylength
            1, // (0xbf) athrow
                    3, // (0xc0) checkcast
                    3, // (0xc1) instanceof
            1, // (0xc2) monitorenter
            1, // (0xc3) monitorexit
        0, // (0xc4) wide
                        4, // (0xc5) multianewarray
                    3, // (0xc6) ifnull
                    3, // (0xc7) ifnonnull
                            5, // (0xc8) goto_w
                            5, // (0xc9) jsr_w
        0
    };


    // ==================== FIELDS ====================

    private final ClassFile   mClass;
    private List<Instruction> mInstructions        = new ArrayList<Instruction>();
    private List<Instruction> mInitialInstructions = new ArrayList<Instruction>();
    private boolean           mIsValid        = true;
    private List<String>      mValidityErrors = new ArrayList<String>();


    // ==================== PUBLIC METHODS ====================

    public ByteCode(ClassFile aClass,
                    byte[]    aCode)
             throws IOException
    {
        mClass = aClass;

        // Parse code.
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(aCode));
        int offset = 0;

        try
        {
            while (0 != in.available())
            {
                Instruction inst = new Instruction(in, offset);
                mInstructions.add(inst);

                int size = inst.size(offset);
                if (0 == size)
                {
                    break;
                }
                else
                {
                    offset += size;
                }
            }
        }
        catch (ClassFileException e)
        {
            mIsValid = false;
            mValidityErrors.add(e.toString());
            mClass.addValidityError(e.toString());
        }

        // Append dummy NOP instruction.
        mInstructions.add(new Instruction());

        // Set the initial code.
        mInitialInstructions.addAll(mInstructions);

        // Update all cross-references (jump target branches).
        for (Instruction inst : mInstructions)
        {
            try
            {
                inst.update();
            }
            catch (ClassFileException e)
            {
                mIsValid = false;
                mValidityErrors.add(String.format("%d: %s", inst.getOffset(), e));
            }
        }

        in.close();
    }

    public void add(int                   aIndex,
                    int                   aOpcode,
                    ConstantPool.Constant aConstant)
             throws ClassFileException
    {
        mInstructions.add(aIndex, new Instruction(aOpcode, aConstant));
    }

    public void store(DataOutputStream aOut)
               throws IOException
    {
        // Store all instructions except for the last dummy NOP instruction.
        int offset = 0;
        ListIterator<Instruction> iterator = mInstructions.listIterator();
        while (iterator.hasNext())
        {
            Instruction inst = iterator.next();
            if (iterator.hasNext())
            {
                inst.store(aOut, offset);
                offset += inst.size(offset);
            }
        }
    }

    public boolean isValid()
    {
        return mIsValid;
    }

    public List<String> getValidityErrors()
    {
        return mValidityErrors;
    }

    public int size()
    {
        // Add the size of all instructions except for the last dummy NOP instruction.
        int size = 0;
        ListIterator<Instruction> iterator = mInstructions.listIterator();
        while (iterator.hasNext())
        {
            Instruction inst = iterator.next();
            if (iterator.hasNext())
            {
                size += inst.size(size);
            }
        }
        return size;
    }

    public int offsetOf(Instruction aInstruction)
    {
        int offset = 0;
        for (Instruction inst : mInstructions)
        {
            if (inst == aInstruction)
            {
                break;
            }
            else
            {
                offset += inst.size(offset);
            }
        }
        return offset;
    }

    public Instruction getByOffset(int aOffset)
    {
        Instruction instruction = null;
        Instruction initialInstruction = null;

        int offset = 0;
        for (Instruction inst : mInstructions)
        {
            if (offset == aOffset)
            {
                instruction = inst;
                break;
            }
            else
            {
                offset += inst.size(offset);
            }
        }
        if (null == instruction)
        {
            return null;
        }

        offset = 0;
        for (Instruction inst : mInitialInstructions)
        {
            if (offset == aOffset)
            {
                initialInstruction = inst;
                break;
            }
            else
            {
                offset += inst.size(offset);
            }
        }
        if ((null == initialInstruction) || (instruction != initialInstruction))
        {
            throw new RuntimeException("ByteCode: attempted to get instruction by offset after byte code has been modified");
        }
 
        return instruction;
    }

    public List<Instruction> getCode()
    {
        return mInstructions;
    }

    public void dump()
    {
        // Store all instructions except for the last dummy NOP instruction.
        int offset = 0;
        ListIterator<Instruction> iterator = mInstructions.listIterator();
        while (iterator.hasNext())
        {
            Instruction inst = iterator.next();
            if (iterator.hasNext())
            {
                System.out.format("%5d: %s\n", offset, inst.toString());
                offset += inst.size(offset);
            }
        }
    }


    // ==================== PRIVATE IMPLEMENTATION ====================

    // ==================== INSTRUCTIONS ====================

    public class Instruction
    {
        private final int           mOpcode;
        private final List<Operand> mOperands = new ArrayList<Operand>();

        private Instruction()
        {
            mOpcode = NOP;
        }

        private Instruction(int                   aOpcode,
                            ConstantPool.Constant aConstant)
                     throws ClassFileException
        {
            if ((aOpcode < 0) || (LAST_OPCODE < aOpcode))
            {
                throw new ClassFileException("ByteCode: invalid instruction opcode " + aOpcode);
            }

            switch (aOpcode)
            {
                case INVOKESTATIC:
                {
                    mOpcode = aOpcode;
                    mOperands.add(new Operand(Operand.CONSTANT_INDEX_16, aConstant));
                    break;
                }
                default:
                {
                    throw new ClassFileException(  "ByteCode: cannot construct instruction\n  "
                                                 + OPCODE_MNEMONIC[aOpcode]
                                                 + " #"
                                                 + aConstant.getIndex()
                                                 + " //"
                                                 + aConstant.toString());
                }
            }
        }

        public Instruction(DataInputStream aIn,
                           int             aOffset)
                    throws IOException, ClassFileException
        {
            // Read the opcode.            
            try
            {
                mOpcode = aIn.readUnsignedByte();
            }
            catch (IOException e)
            {
                throw new IOException("ByteCode: IOException reading opcode at offset " + aOffset + ": " + e);
            }

            // Check the opcode.
            if (mOpcode > LAST_OPCODE)
            {
                throw new ClassFileException("ByteCode: invalid instruction opcode " + mOpcode + " at offset " + aOffset);
            }

            // Read the operands.
            try
            {
                switch (mOpcode)
                {
                    case BIPUSH:
                    {
                        mOperands.add(new Operand(Operand.SIGNED_INTEGER_8, aIn));
                        break;
                    }
                    case SIPUSH:
                    {
                        mOperands.add(new Operand(Operand.SIGNED_INTEGER_16, aIn));
                        break;
                    }
                    case NEWARRAY:
                    {
                        mOperands.add(new Operand(Operand.UNSIGNED_INTEGER_8, aIn));
                        break;
                    }
                    case ILOAD:
                    case LLOAD:
                    case FLOAD:
                    case DLOAD:
                    case ALOAD:
                    case ISTORE:
                    case LSTORE:
                    case FSTORE:
                    case DSTORE:
                    case ASTORE:
                    case RET:
                    {
                        mOperands.add(new Operand(Operand.LOCAL_INDEX_8, aIn));
                        break;
                    }
                    case LDC:
                    {
                        Operand op = new Operand(Operand.CONSTANT_INDEX_8, aIn);
                        int opType = op.getConstantType();
                        if (   (ConstantPool.INTEGER == opType)
                            || (ConstantPool.FLOAT   == opType)
                            || (ConstantPool.STRING  == opType))
                        {
                            mOperands.add(op);
                        }
                        else
                        {
                            throw new ClassFileException();
                        }
                        break;
                    }
                    case LDC_W:
                    {
                        Operand op = new Operand(Operand.CONSTANT_INDEX_16, aIn);
                        int opType = op.getConstantType();
                        if (   (ConstantPool.INTEGER == opType)
                            || (ConstantPool.FLOAT   == opType)
                            || (ConstantPool.STRING  == opType))
                        {
                            mOperands.add(op);
                        }
                        else
                        {
                            throw new ClassFileException();
                        }
                        break;
                    }
                    case LDC2_W:
                    {
                        Operand op = new Operand(Operand.CONSTANT_INDEX_16, aIn);
                        int opType = op.getConstantType();
                        if (   (ConstantPool.LONG == opType)
                            || (ConstantPool.DOUBLE  == opType))
                        {
                            mOperands.add(op);
                        }
                        else
                        {
                            throw new ClassFileException();
                        }
                        break;
                    }
                    case NEW:
                    case ANEWARRAY:
                    case CHECKCAST:
                    case INSTANCEOF:
                    {
                        Operand op = new Operand(Operand.CONSTANT_INDEX_16, aIn);
                        if (ConstantPool.CLASS == op.getConstantType())
                        {
                            mOperands.add(op);
                        }
                        else
                        {
                            throw new ClassFileException();
                        }
                        break;
                    }
                    case GETSTATIC:
                    case PUTSTATIC:
                    case GETFIELD:
                    case PUTFIELD:
                    {
                        Operand op = new Operand(Operand.CONSTANT_INDEX_16, aIn);
                        if (ConstantPool.FIELDREF == op.getConstantType())
                        {
                            mOperands.add(op);
                        }
                        else
                        {
                            throw new ClassFileException();
                        }
                        break;
                    }
                    case INVOKEVIRTUAL:
                    case INVOKESPECIAL:
                    case INVOKESTATIC:
                    {
                        Operand op = new Operand(Operand.CONSTANT_INDEX_16, aIn);
                        if (ConstantPool.METHODREF == op.getConstantType())
                        {
                            mOperands.add(op);
                        }
                        else
                        {
                            throw new ClassFileException();
                        }
                        break;
                    }
                    case IFEQ:
                    case IFNE:
                    case IFLT:
                    case IFGE:
                    case IFGT:
                    case IFLE:
                    case IF_ICMPEQ:
                    case IF_ICMPNE:
                    case IF_ICMPLT:
                    case IF_ICMPGE:
                    case IF_ICMPGT:
                    case IF_ICMPLE:
                    case IF_ACMPEQ:
                    case IF_ACMPNE:
                    case IFNULL:
                    case IFNONNULL:
                    case GOTO:
                    case JSR:
                    {
                        mOperands.add(new Operand(Operand.OPCODE_OFFSET_16, aIn));
                        break;
                    }
                    case GOTO_W:
                    case JSR_W:
                    {
                        mOperands.add(new Operand(Operand.OPCODE_OFFSET_32, aIn));
                        break;
                    }
                    case IINC:
                    {
                        mOperands.add(new Operand(Operand.LOCAL_INDEX_8, aIn));
                        mOperands.add(new Operand(Operand.SIGNED_INTEGER_8, aIn));
                        break;
                    }
                    case MULTIANEWARRAY:
                    {
                        Operand op1 = new Operand(Operand.CONSTANT_INDEX_16, aIn);
                        if (ConstantPool.CLASS == op1.getConstantType())
                        {
                            mOperands.add(op1);
                        }
                        else
                        {
                            throw new ClassFileException();
                        }

                        Operand op2 = new Operand(Operand.UNSIGNED_INTEGER_8, aIn);
                        mOperands.add(op2);
                        break;
                    }
                    case INVOKEINTERFACE:
                    {
                        Operand op1 = new Operand(Operand.CONSTANT_INDEX_16, aIn);
                        if (ConstantPool.INTERFACEMETHODREF == op1.getConstantType())
                        {
                            mOperands.add(op1);
                        }
                        else
                        {
                            throw new ClassFileException();
                        }

                        Operand op2 = new Operand(Operand.UNSIGNED_INTEGER_8, aIn);
                        mOperands.add(op2);

                        Operand op3 = new Operand(Operand.UNSIGNED_INTEGER_8, aIn);
                        mOperands.add(op3);
                        break;
                    }
                    case LOOKUPSWITCH:
                    {
                        int paddingSize = (aOffset & ~0x03) + 4 - aOffset - 1;
                        aIn.skip(paddingSize);

                        Operand defaultOffset = new Operand(Operand.OPCODE_OFFSET_32, aIn);
                        mOperands.add(defaultOffset);

                        Operand nbOffsets = new Operand(Operand.SIGNED_INTEGER_32, aIn);
                        mOperands.add(nbOffsets);
                        for (int i = 0; i < nbOffsets.getInteger(); ++i)
                        {
                            mOperands.add(new Operand(Operand.SIGNED_INTEGER_32, aIn));
                            mOperands.add(new Operand(Operand.OPCODE_OFFSET_32, aIn));
                        }

                        break;
                    }
                    case TABLESWITCH:
                    {
                        int paddingSize = (aOffset & ~0x03) + 4 - aOffset - 1;
                        aIn.skip(paddingSize);

                        Operand defaultOffset = new Operand(Operand.OPCODE_OFFSET_32, aIn);
                        mOperands.add(defaultOffset);

                        Operand low = new Operand(Operand.SIGNED_INTEGER_32, aIn);
                        mOperands.add(low);

                        Operand high = new Operand(Operand.SIGNED_INTEGER_32, aIn);
                        mOperands.add(high);

                        int nbOffsets = high.getInteger() - low.getInteger() + 1;
                        for (int i = 0; i < nbOffsets; ++i)
                        {
                            mOperands.add(new Operand(Operand.OPCODE_OFFSET_32, aIn));
                        }

                        break;
                    }
                    case WIDE:
                    {
                        Operand op1 = new Operand(Operand.SUB_OPCODE, aIn);
                        int subOpcode = op1.getSubOpcode();
                        if (   (ILOAD == subOpcode)
                            || (LLOAD == subOpcode)
                            || (FLOAD == subOpcode)
                            || (DLOAD == subOpcode)
                            || (ALOAD == subOpcode)
                            || (ISTORE == subOpcode)
                            || (LSTORE == subOpcode)
                            || (FSTORE == subOpcode)
                            || (DSTORE == subOpcode)
                            || (ASTORE == subOpcode)
                            || (RET == subOpcode)
                            || (IINC == subOpcode))
                        {
                            mOperands.add(op1);
                        }
                        else
                        {
                            throw new ClassFileException();
                        }

                        Operand op2 = new Operand(Operand.LOCAL_INDEX_16, aIn);
                        mOperands.add(op2);

                        if (IINC == subOpcode)
                        {
                            Operand op3 = new Operand(Operand.SIGNED_INTEGER_16, aIn);
                            mOperands.add(op3);
                        }
                        break;
                    }
                }                
            }
            catch (ClassFileException e)
            {
                throw new ClassFileException(  "Invalid operand type for opcode " + OPCODE_MNEMONIC[mOpcode]
                                             + " at offset " + aOffset);
            }
            catch (IOException e)
            {
                throw new IOException(  "IOException reading operands for opcode " + OPCODE_MNEMONIC[mOpcode]
                                      + " at offset " + aOffset
                                      + ": " + e);
            }
        }

        public void update()
                    throws ClassFileException
        {
            for (Operand op : mOperands)
            {
                op.update();
            }
        }

        public void store(DataOutputStream aOut,
                          int              aOffset)
                   throws IOException
        {
            aOut.writeByte(mOpcode);

            if ((LOOKUPSWITCH == mOpcode) || (TABLESWITCH == mOpcode))
            {
                int paddingSize = (aOffset & ~0x03) + 4 - aOffset - 1;
                for (int i = 0; i < paddingSize; ++i)
                {
                    aOut.writeByte(0);
                }
            }

            for (Operand op : mOperands)
            {
                op.store(aOut);
            }
        }

        public int size(int aOffset)
        {
            if ((LOOKUPSWITCH == mOpcode) || (TABLESWITCH == mOpcode))
            {
                int paddingSize = (aOffset & ~0x03) + 4 - aOffset - 1;
                return 1 + paddingSize + (mOperands.size() * 4);
            }
            else if (WIDE == mOpcode)
            {
                try
                {
                    if (IINC == mOperands.get(0).getSubOpcode())
                    {
                        return 6;
                    }
                    else
                    {
                        return 4;
                    }
                }
                catch (ClassFileException e)
                {
                    throw new RuntimeException(e.getMessage());
                }
            }
            else
            {
                return INSTRUCTION_SIZE[mOpcode];
            }
        }

        public int getOffset()
        {
            return offsetOf(this);
        }

        public int getOpcode()
        {
            return mOpcode;
        }

        public List<Operand> getOperands()
        {
            return mOperands;
        }

        public String toString()
        {
            String string = null;

            if (mOpcode <= LAST_OPCODE)
            {
                string = OPCODE_MNEMONIC[mOpcode];
            }
            else
            {
                string = "INVALID_OPCODE";
            }
            while (string.length() < 15) {
                string = string + (' ');
            }

            switch (mOpcode)
            {
                case BIPUSH:
                case SIPUSH:
                case ILOAD:
                case LLOAD:
                case FLOAD:
                case DLOAD:
                case ALOAD:
                case ISTORE:
                case LSTORE:
                case FSTORE:
                case DSTORE:
                case ASTORE:
                case IINC:
                case IFEQ:
                case IFNE:
                case IFLT:
                case IFGE:
                case IFGT:
                case IFLE:
                case IF_ICMPEQ:
                case IF_ICMPNE:
                case IF_ICMPLT:
                case IF_ICMPGE:
                case IF_ICMPGT:
                case IF_ICMPLE:
                case IF_ACMPEQ:
                case IF_ACMPNE:
                case IFNULL:
                case IFNONNULL:
                case GOTO:
                case JSR:
                case GOTO_W:
                case JSR_W:
                case RET:
                case NEWARRAY:
                case WIDE:
                {
                    for (Operand op : mOperands)
                    {
                        string = string + " " + op.toValue();
                    }
                    break;
                }
                case LDC:
                case LDC_W:
                case LDC2_W:
                case GETSTATIC:
                case PUTSTATIC:
                case GETFIELD:
                case PUTFIELD:
                case INVOKEVIRTUAL:
                case INVOKESPECIAL:
                case INVOKESTATIC:
                case INVOKEINTERFACE:
                case NEW:
                case ANEWARRAY:
                case CHECKCAST:
                case INSTANCEOF:
                {
                    Operand op = mOperands.get(0);
                    string = string + " " + op.toValue() + " //" + op.toString();
                    break;
                }
                case MULTIANEWARRAY:
                {
                    for (Operand op : mOperands)
                    {
                        string = string + " " + op.toValue();
                    }
                    string = string + " " + " //" + mOperands.get(1).toString();
                    break;
                }
                case TABLESWITCH:
                {
                    break;
                }
                case LOOKUPSWITCH:
                {
                    break;
                }
            }

            return string;
        }

        // ==================== OPERANDS ====================

        public class Operand
        {
            private static final int SIGNED_INTEGER_8     = 1;
            private static final int SIGNED_INTEGER_16    = 2;
            private static final int SIGNED_INTEGER_32    = 3;
            private static final int UNSIGNED_INTEGER_8   = 4;
            //private static final int UNSIGNED_INTEGER_16  = 5; // unused
            //private static final int UNSIGNED_INTEGER_32  = 6; // unused
            private static final int LOCAL_INDEX_8        = 7;
            private static final int LOCAL_INDEX_16       = 8;
            private static final int CONSTANT_INDEX_8     = 9;
            private static final int CONSTANT_INDEX_16    = 10;
            private static final int OPCODE_OFFSET_16     = 11;
            private static final int OPCODE_OFFSET_32     = 12;
            private static final int SUB_OPCODE           = 100;

            private final int             mType;
            private int                   mValue;
            private ConstantPool.Constant mConstant = null;
            private Instruction           mTargetBranch = null;

            private Operand(int                   aType,
                            ConstantPool.Constant aConstant)
                     throws ClassFileException
            {
                if ((CONSTANT_INDEX_8 != aType) && (CONSTANT_INDEX_16 != aType))
                {
                    throw new ClassFileException(  "ByteCode: invalid operand type "
                                                 + aType
                                                 + " for constant index operand");
                }

                mType = aType;
                mConstant = aConstant;
            }

            public Operand(int             aType,
                           DataInputStream aIn)
                    throws IOException, ClassFileException
            {
                mType = aType;
                switch (mType)
                {
                    case SIGNED_INTEGER_8:
                        mValue = aIn.readByte();
                        break;
                    case SIGNED_INTEGER_16:
                        mValue = aIn.readShort();
                        break;
                    case SIGNED_INTEGER_32:
                        mValue = aIn.readInt();
                        break;
                    case UNSIGNED_INTEGER_8:
                    case LOCAL_INDEX_8:
                    case SUB_OPCODE:
                        mValue = aIn.readUnsignedByte();
                        break;
                    case LOCAL_INDEX_16:
                        mValue = aIn.readUnsignedShort();
                        break;
                    case CONSTANT_INDEX_8:
                        mValue = aIn.readUnsignedByte();
                        mConstant = ByteCode.this.mClass.getConstantPool().getByIndex(mValue);
                        break;
                    case CONSTANT_INDEX_16:
                        mValue = aIn.readUnsignedShort();
                        mConstant = ByteCode.this.mClass.getConstantPool().getByIndex(mValue);
                        break;
                    case OPCODE_OFFSET_16:
                        mValue = aIn.readShort();
                        break;
                    case OPCODE_OFFSET_32:
                        mValue = aIn.readInt();
                        break;
                    default:
                        throw new ClassFileException("ByteCode: attempted to get unknown operand type " + mType);
                }
            }

            public void update()
                        throws ClassFileException
            {
                if ((OPCODE_OFFSET_16 == mType) || (OPCODE_OFFSET_32 == mType))
                {
                    mTargetBranch = ByteCode.this.getByOffset(Instruction.this.getOffset() + mValue);
                }
            }

            public void store(DataOutputStream aOut)
                       throws IOException
            {
                switch (mType)
                {
                    case SIGNED_INTEGER_8:
                    case UNSIGNED_INTEGER_8:
                    case LOCAL_INDEX_8:
                    case SUB_OPCODE:
                    {
                        aOut.writeByte(mValue);
                        break;
                    }
                    case SIGNED_INTEGER_16:
                    case LOCAL_INDEX_16:
                    {
                        aOut.writeShort(mValue);
                        break;
                    }
                    case SIGNED_INTEGER_32:
                    {
                        aOut.writeInt(mValue);
                        break;
                    }
                    case CONSTANT_INDEX_8:
                    {
                        int constantIndex = mConstant.getIndex();
                        if (constantIndex != mValue)
                        {
                            System.out.format("%5d: %s\n", Instruction.this.getOffset(), Instruction.this.toString());
                            System.out.format("    => changed #%d to #%d\n", mValue, constantIndex);
                        }
                        if (0 != (constantIndex & 0xFFFFFF00))
                        {
                            throw new RuntimeException(String.format("ByteCode: %d: %s #%d: operand does not fit in byte",
                                                                     Instruction.this.getOffset(),
                                                                     OPCODE_MNEMONIC[Instruction.this.mOpcode],
                                                                     constantIndex));
                        }
                        aOut.writeByte(constantIndex);
                        break;
                    }
                    case CONSTANT_INDEX_16:
                    {
                        int constantIndex = mConstant.getIndex();
                        if (constantIndex != mValue)
                        {
                            System.out.format("%5d: %s\n", Instruction.this.getOffset(), Instruction.this.toString());
                            System.out.format("    => changed #%d to #%d\n", mValue, constantIndex);
                        }
                        if (0 != (constantIndex & 0xFFFF0000))
                        {
                            throw new RuntimeException(String.format("ByteCode: %d: %s #%d -> operand does not fit in short",
                                                                     Instruction.this.getOffset(),
                                                                     OPCODE_MNEMONIC[Instruction.this.mOpcode],
                                                                     constantIndex));
                        }
                        aOut.writeShort(constantIndex);
                        break;
                    }
                    case OPCODE_OFFSET_16:
                    {
                        int opcodeOffset = mTargetBranch.getOffset() - Instruction.this.getOffset();
                        if (opcodeOffset != mValue)
                        {
                            System.out.format("%5d: %s\n", Instruction.this.getOffset(), Instruction.this.toString());
                            System.out.format("    => changed %d: to %d:\n", mValue, opcodeOffset);
                        }
                        if (   (0 != (opcodeOffset & 0xFFFF0000))
                            && (0xFFFF0000 != (opcodeOffset & 0xFFFF0000)))
                        {
                            throw new RuntimeException(String.format("ByteCode: %d: %s %d: -> operand does not fit in short",
                                                                     Instruction.this.getOffset(),
                                                                     OPCODE_MNEMONIC[Instruction.this.mOpcode],
                                                                     opcodeOffset));
                        }
                        aOut.writeShort(opcodeOffset);
                        break;
                    }
                    case OPCODE_OFFSET_32:
                    {
                        int opcodeOffset = mTargetBranch.getOffset() - Instruction.this.getOffset();
                        if (opcodeOffset != mValue)
                        {
                            System.out.format("%5d: %s\n", Instruction.this.getOffset(), Instruction.this.toString());
                            System.out.format("    => changed %d: to %d:\n", mValue, opcodeOffset);
                        }
                        aOut.writeInt(opcodeOffset);
                        break;
                    }
                }
            }

            public int getInteger()
                             throws ClassFileException
            {
                if (   (SIGNED_INTEGER_8   == mType)
                    || (SIGNED_INTEGER_16  == mType)
                    || (SIGNED_INTEGER_32  == mType)
                    || (UNSIGNED_INTEGER_8 == mType))
                {
                    return mValue;
                }
                else
                {
                    throw new ClassFileException("ByteCode: cannot get integer from operand type " + mType);
                }
            }

            public int getConstantIndex()
                                      throws ClassFileException
            {
                if ((CONSTANT_INDEX_8 == mType) || (CONSTANT_INDEX_16 == mType))
                {
                    return mValue;
                }
                else
                {
                    throw new ClassFileException("ByteCode: cannot get constant index from operand type " + mType);
                }
            }

            public int getConstantType()
                                throws ClassFileException
            {
                if ((CONSTANT_INDEX_8 == mType) || (CONSTANT_INDEX_16 == mType))
                {
                    return mConstant.getType();
                }
                else
                {
                    throw new ClassFileException("ByteCode: cannot get constant type from operand type " + mType);
                }
            }

            public int getSubOpcode()
                             throws ClassFileException
            {
                if (SUB_OPCODE == mType)
                {
                    return mValue;
                }
                else
                {
                    throw new ClassFileException("ByteCode: cannot get sub-opcode from operand type " + mType);
                }
            }

            public String toValue()
            {
                String value = null;
                switch (mType)
                {
                    case SIGNED_INTEGER_8:
                    case SIGNED_INTEGER_16:
                    case SIGNED_INTEGER_32:
                    case UNSIGNED_INTEGER_8:
                    case LOCAL_INDEX_8:
                    case LOCAL_INDEX_16:
                        value = new Integer(mValue).toString();
                        break;
                    case CONSTANT_INDEX_8:
                    case CONSTANT_INDEX_16:
                        value = String.format("#%d", mConstant.getIndex());
                        break;
                    case OPCODE_OFFSET_16:
                    case OPCODE_OFFSET_32:
                        value = String.format("%d:", mTargetBranch.getOffset());
                        break;
                    case SUB_OPCODE:
                        value = OPCODE_MNEMONIC[mValue];
                        break;
                    default:
                        throw new RuntimeException("ByeCode: attempted to get value from unknown operand type " + mType);
                }
                return value;
            }

            public String toString()
            {
                if ((CONSTANT_INDEX_8 == mType) || (CONSTANT_INDEX_16 == mType))
                {
                    return mConstant.toString();
                }
                else
                {
                    return toValue();
                }
            }
        }
    }
}
