/* Copyright (c) 2006 Olivier Elshocht
 *
 * ConstantPool.java
 *
 * Created on 19 novembre 2006, 20:52
 */

package classfile;

import java.io.*;
import java.util.*;
import security.*;
import utf8.Utf8;

/**
 *
 * @author Olivier Elshocht
 */
public class ConstantPool
{
    // ==================== CONSTANT TYPES ====================

    public static final int UTF8               =  1;
    public static final int INTEGER            =  3;
    public static final int FLOAT              =  4;
    public static final int LONG               =  5;
    public static final int DOUBLE             =  6;
    public static final int CLASS              =  7;
    public static final int STRING             =  8;
    public static final int FIELDREF           =  9;
    public static final int METHODREF          = 10;
    public static final int INTERFACEMETHODREF = 11;
    public static final int NAMEANDTYPE        = 12;

    public static final int LAST_CONSTANT_TYPE = 12;

    // ==================== CONSTANT TYPE NAMES ====================

    private static final String[] TYPE_NAME =
    {
        "???",                //  O
        "Utf8",               //  1
        "???",                //  2
        "Integer",            //  3
        "Float",              //  4
        "Long",               //  5
        "Double",             //  6
        "Class",              //  7
        "String",             //  8
        "FieldRef",           //  9
        "MethodRef",          // 10
        "InterfaceMethodRef", // 11
        "NameAndType"         // 12
    };

    // ==================== CONSTANT SORTING ORDER ====================

    // The only opcode taking an 8-bit constant index operand is LDC.
    // The only valid constant types for LDC are Integer, Float and String
    // ==> these should be first in the sorting order.
    //  0: Class
    //     Integer
    //     Float
    //     String
    //  4: Long
    //     Double
    //  6: FieldRef
    //  7: MethodRef
    //  8: InterfaceMethodRef
    //  9: NameAndType
    // 10: Utf8
    private static final int[] SORTING_ORDER =
    {
        100, // ???
         10, // Utf8
        100, // ???
          1, // Integer
          1, // Float
          4, // Long
          4, // Double
          1, // Class
          0, // String
          6, // FieldRef
          7, // MethodRef
          8, // InterfaceMethodRef
          9  // NameAndType
    };


    // ==================== FIELDS ====================

    private final List<Constant> mPool           = new ArrayList<Constant>();
    private final List<Constant> mInitialPool    = new ArrayList<Constant>();
    private boolean              mIsValid        = true;
    private List<String>         mValidityErrors = new ArrayList<String>();


    // ==================== PUBLIC INTERFACE ====================

    public ConstantPool(int             aCount,
                        DataInputStream aIn)
                 throws IOException
    {
        // Parse all constants.
        try
        {
            // Constant 0 is implicitly to Object.
            mPool.add(new Constant(0));

            // Parse other constants.
            for (int i = 1; i < aCount; ++i)
            {
                mPool.add(parseConstant(aIn));
            }
        }
        catch (ClassFileException e)
        {
            mIsValid = false;
            mValidityErrors.add(e.toString());
        }

        // Set the intial pool.
        mInitialPool.addAll(mPool);

        // Skip the first implicit constant Object.
        ListIterator<Constant> iterator = mPool.listIterator();
        if (iterator.hasNext())
        {
            iterator.next();
        }

        // Update all cross-references.
        while (iterator.hasNext())
        {
            Constant constant = iterator.next();
            try
            {
                constant.update();
            }
            catch (ClassFileException e)
            {
                mIsValid = false;
                mValidityErrors.add(String.format("#%d = (%2d: %s) %s: %s",
                                                   constant.getIndex(),
                                                   constant.getType(),
                                                   constant.getTypeName(),
                                                   constant.toValue(),
                                                   e));
            }
        }
    }

    public boolean isValid()
    {
        return mIsValid;
    }

    public List<String> getValidityErrors()
    {
        return new ArrayList<String>(mValidityErrors);
    }

    public void store(DataOutputStream aOut)
               throws IOException, ClassFileException
    {
        if (!mIsValid)
        {
            throw new ClassFileException("Cannot store invalid constant pool");
        }

        // First shuffle all constants, then sorting them in oder.
        Constant first = mPool.remove(0);
        Collections.shuffle(mPool);
        Collections.sort(mPool, new Comparator<Constant>()
                                {
                                    public int compare(Constant c1, Constant c2)
                                    {
                                        return SORTING_ORDER[c1.mTag] - SORTING_ORDER[c2.mTag];
                                    }
                                });
        mPool.add(0, first);

        // Skip the first implicit constant Object.
        ListIterator<Constant> iterator = mPool.listIterator();
        if (iterator.hasNext())
        {
            iterator.next();
        }

        // Store all constants.
        while (iterator.hasNext())
        {
            iterator.next().store(aOut);
        }
    }

    public int size()
    {
        return mPool.size();
    }

    public int indexOf(Constant aConstant)
    {
        return mPool.indexOf(aConstant);
    }

    public Constant getByIndex(int aIndex)
    {
        Constant constant = null;
        Constant initialConstant = null;

        try
        {
            constant = mPool.get(aIndex);
        }
        catch (IndexOutOfBoundsException e)
        {
            return null;
        }

        try
        {
            initialConstant = mInitialPool.get(aIndex);
        }
        catch (IndexOutOfBoundsException e)
        {
            throw new RuntimeException("ConstantPool: attempted to get constant by index after pool has been modified");
        }

        if (constant != initialConstant)
        {
            throw new RuntimeException("ConstantPool: attempted to get constant by index after pool has been modified");            
        }

        return constant;
    }

    public List<Constant> getByType(int aType)
    {
        List<Constant> list = new ArrayList<Constant>();
        for (Constant constant : mPool)
        {
            if (aType == constant.getType())
            {
                list.add(constant);
            }
        }
        return list;
    }

    public ConstantUtf8 addUtf8(String aString)
    {
        ConstantUtf8 newUtf8= new ConstantUtf8(aString);
        mPool.add(newUtf8);
        return newUtf8;
    }

    public ConstantClass addClass(ConstantUtf8 aName)
    {
        ConstantClass newClass = new ConstantClass(aName);
        mPool.add(newClass);
        if (!mPool.contains(aName))
        {
            mPool.add(aName);
        }
        return newClass;
    }

    public ConstantMethodRef addMethodRef(ConstantClass       aClass,
                                          ConstantNameAndType aNameAndType)
    {
        ConstantMethodRef newMethodRef = new ConstantMethodRef(aClass, aNameAndType);
        mPool.add(newMethodRef);
        if (!mPool.contains(aClass))
        {
            mPool.add(aClass);
        }
        if (!mPool.contains(aNameAndType))
        {
            mPool.add(aNameAndType);
        }
        return newMethodRef;
    }

    public ConstantNameAndType addNameAndType(ConstantUtf8 aName,
                                              ConstantUtf8 aDescriptor)
    {
        ConstantNameAndType newNameAndType = new ConstantNameAndType(aName, aDescriptor);
        mPool.add(newNameAndType);
        if (!mPool.contains(aName))
        {
            mPool.add(aName);
        }
        if (!mPool.contains(aDescriptor))
        {
            mPool.add(aDescriptor);
        }
        return newNameAndType;
    }

    public void cryptStrings()
    {
        for (Constant constant : getByType(STRING))
        {
            if (constant instanceof ConstantString)
            {
                ConstantString constantString = (ConstantString) constant;
                System.out.println("Crypting " + constantString.toString());
                constantString.crypt();
                System.out.println("=> " + constantString.toString());
            }
            else
            {
                throw new RuntimeException("ConstantPool: constant type mismatch");
            }
        }
    }

    public void dump()
    {
        // Skip the first implicit constant Object.
        ListIterator<Constant> iterator = mPool.listIterator();
        if (iterator.hasNext())
        {
            iterator.next();
        }

        // Dump all constants.
        while (iterator.hasNext())
        {
            iterator.next().dump();
        }
    }


    // ==================== PRIVATE IMPLEMENTATION ====================

    private Constant parseConstant(DataInputStream in)
                            throws IOException, ClassFileException
    {
        int tag = in.readUnsignedByte();
        switch (tag) {
            case UTF8:
                return new ConstantUtf8(tag, in);
            case INTEGER:
                return new ConstantInteger(tag, in);
            case FLOAT:
                return new ConstantFloat(tag, in);
            case LONG:
                return new ConstantLong(tag, in);
            case DOUBLE:
                return new ConstantDouble(tag, in);
            case CLASS:
                return new ConstantClass(tag, in);
            case STRING:
                return new ConstantString(tag, in);
            case FIELDREF:
                return new ConstantFieldRef(tag, in);
            case METHODREF:
                return new ConstantMethodRef(tag, in);
            case INTERFACEMETHODREF:
                return new ConstantInterfaceMethodRef(tag,in);
            case NAMEANDTYPE:
                return new ConstantNameAndType(tag, in);
            default:
                return new Constant(tag);
        }
    }


    // ==================== CONSTANT CLASSES ====================

    public class Constant
    {
        private final int mTag;
        private int       mRefCount = 0; 

        protected Constant(int aTag)
        {
            mTag = aTag;
        }

        protected void update()
                       throws ClassFileException
        {
            throw new ClassFileException("ConstantPool: invalid constant tag " + mTag);
        }

        protected void store(DataOutputStream aOut)
                    throws IOException
        {
            aOut.writeByte(mTag);
        }

        public final void incRefCount()
        {
            ++mRefCount;
        }

        public final void decRefCount()
        {
            --mRefCount;
            if (0 == mRefCount)
            {
                System.out.println("=> Removing " + toString());
                mPool.remove(this);
            }
        }

        public final int getRefCount()
        {
            return mRefCount;
        }

        public final int getIndex()
        {
            return ConstantPool.this.indexOf(this);
        }

        public final int getType()
        {
            return mTag;
        }

        public final String getTypeName()
        {
            try
            {
                return TYPE_NAME[mTag];
            }
            catch (IndexOutOfBoundsException e)
            {
                return "???";
            }
        }

        public String toValue()
        {
            return toString();
        }

        public String toString()
        {
            return "???";
        }

        public final void dump()
        {
            System.out.format("#%d = (%2d: %s)\t%s", getIndex(), mTag, getTypeName(), toValue());
            switch (getType())
            {
                case 0:
                case UTF8:
                case INTEGER:
                case FLOAT:
                case LONG:
                case DOUBLE:
                    System.out.format("\n");
                    break;
                default:
                    System.out.format(" //%s\n", toString());
            }
        }
    }

    public class ConstantUtf8 extends Constant
    {
        private String mValue;

        protected ConstantUtf8(String aValue)
        {
            super(UTF8);
            mValue = aValue;
        }

        protected ConstantUtf8(int             aTag,
                               DataInputStream aIn)
                        throws IOException, ClassFileException
        {
            super(aTag);
            mValue = aIn.readUTF();
        }

        protected void update()
        {
            // Empty.
        }

        protected void store(DataOutputStream aOut)
                      throws IOException
        {
            super.store(aOut);
            aOut.writeUTF(mValue);
        }

        public String toString()
        {
            return mValue;
        }
    }

    public class ConstantInteger extends Constant
    {
        private final int mValue;

        protected ConstantInteger(int             aTag,
                                  DataInputStream aIn)
                           throws IOException
        {
            super(aTag);
            mValue = aIn.readInt();
        }

        protected void update()
        {
            // Empty.
        }

        protected void store(DataOutputStream aOut)
                      throws IOException
        {
            super.store(aOut);
            aOut.writeInt(mValue);
        }

        public String toValue()
        {
            return new Integer(mValue).toString();
        }

        public String toString()
        {
            return "int " +  toValue();
        }
    }

    public class ConstantLong extends Constant
    {
        private final long mValue;

        protected ConstantLong(int             aTag,
                               DataInputStream aIn)
                        throws IOException
        {
            super(aTag);
            mValue = aIn.readLong();
        }

        protected void update()
        {
            // Empty.
        }

        protected void store(DataOutputStream aOut)
                      throws IOException
        {
            super.store(aOut);
            aOut.writeLong(mValue);
        }

        public String toValue()
        {
            return new Long(mValue).toString();
        }

        public String toString()
        {
            return "long " +  toValue();
        }
    }

    public class ConstantFloat extends Constant
    {
        private final float mValue;

        protected ConstantFloat(int             aTag,
                                DataInputStream aIn)
                         throws IOException
        {
            super(aTag);
            mValue = aIn.readFloat();
        }

        protected void update()
        {
            // Empty.
        }

        protected void store(DataOutputStream aOut)
                      throws IOException
        {
            super.store(aOut);
            aOut.writeFloat(mValue);
        }

        public String toValue()
        {
            return new Float(mValue).toString();
        }

        public String toString()
        {
            return "float " +  toValue();
        }
    }

    public class ConstantDouble extends Constant
    {
        private final double mValue;

        protected ConstantDouble(int             aTag,
                                 DataInputStream aIn)
                          throws IOException
        {
            super(aTag);
            mValue = aIn.readDouble();
        }

        protected void update()
        {
            // Empty.
        }

        protected void store(DataOutputStream aOut)
                      throws IOException
        {
            super.store(aOut);
            aOut.writeDouble(mValue);
        }

        public String toValue()
        {
            return new Double(mValue).toString();
        }

        public String toString()
        {
            return "double " + toValue();
        }
    }

    public class ConstantClass extends Constant
    {
        private final int    name_index;
        private ConstantUtf8 mName = null;

        private ConstantClass(ConstantUtf8 aName)
        {
            super(CLASS);
            this.name_index = 0;
            mName = aName;
            mName.incRefCount();
        }

        protected ConstantClass(int             aTag,
                                DataInputStream aIn)
                         throws IOException
        {
            super(aTag);
            this.name_index = aIn.readUnsignedShort();
        }

        protected void update()
                       throws ClassFileException
        {
            Constant constantName = getByIndex(this.name_index);
            if (constantName instanceof ConstantUtf8)
            {
                mName = (ConstantUtf8)constantName;
                mName.incRefCount();
            }
            else
            {
                throw new ClassFileException("Invalid constant reference");
            }
        }

        protected void store(DataOutputStream aOut)
                      throws IOException
        {
            super.store(aOut);
            aOut.writeShort(mName.getIndex());
        }

        public String toValue()
        {
            return "#" + mName.getIndex();
        }

        public String toString()
        {
            return mName.toString();
        }
    }

    public class ConstantString extends Constant
    {
        private final int    string_index;
        private ConstantUtf8 mString = null;

        protected ConstantString(int             aTag,
                                 DataInputStream aIn)
                          throws IOException
        {
            super(aTag);
            this.string_index = aIn.readUnsignedShort();
        }

        protected void update()
                       throws ClassFileException
        {
            Constant constantString = getByIndex(this.string_index);
            if (constantString instanceof ConstantUtf8)
            {
                mString = (ConstantUtf8)constantString;
                mString.incRefCount();
            }
            else
            {
                throw new ClassFileException("Invalid constant reference");
            }
        }

        protected void store(DataOutputStream aOut)
                      throws IOException
        {
            super.store(aOut);
            aOut.writeShort(mString.getIndex());
        }

        public void crypt()
        {
            String oldValue = mString.mValue;
            String newValue = Utf8.utf8(oldValue);

            mString.decRefCount();
            mString = addUtf8(newValue);
            mString.incRefCount();
        }

        public void setString(ConstantUtf8 aString)
        {
            if (null != mString)
            {
                mString.decRefCount();
            }
            mString = aString;
            mString.incRefCount();
        }

        public String toValue()
        {
            return "#" + mString.getIndex();
        }

        public String toString()
        {
            return "String \"" + mString.toString() + "\"";
        }
    }

    private abstract class ConstantRef extends Constant
    {
        private final int           class_index;
        private final int           name_and_type_index;
        private ConstantClass       mClass = null;
        private ConstantNameAndType mNameAndType = null;

        protected ConstantRef(int                 aTag,
                              ConstantClass       aClass,
                              ConstantNameAndType aNameAndType)
        {
            super(aTag);
            this.class_index = 0;
            this.name_and_type_index = 0;
            mClass = aClass;
            mNameAndType = aNameAndType;
            mClass.incRefCount();
            mNameAndType.incRefCount();
        }

        protected ConstantRef(int             aTag,
                              DataInputStream aIn)
                       throws IOException
        {
            super(aTag);
            this.class_index = aIn.readUnsignedShort();
            this.name_and_type_index = aIn.readUnsignedShort();
        }

        protected void update()
                       throws ClassFileException
        {
            Constant constantClass       = getByIndex(this.class_index);
            Constant constantNameAndType = getByIndex(this.name_and_type_index);
            if ((constantClass instanceof ConstantClass) && (constantNameAndType instanceof ConstantNameAndType))
            {
                mClass       = (ConstantClass)constantClass;
                mNameAndType = (ConstantNameAndType)constantNameAndType;
                mClass.incRefCount();
                mNameAndType.incRefCount();
            }
            else
            {
                throw new ClassFileException("Invalid constant reference");
            }
        }

        protected void store(DataOutputStream aOut)
                      throws IOException
        {
            super.store(aOut);
            aOut.writeShort(mClass.getIndex());
            aOut.writeShort(mNameAndType.getIndex());
        }

        public String toValue()
        {
            return "#" + mClass.getIndex() + ".#" + mNameAndType.getIndex();
        }

        public String toString()
        {
            return getTypeName() + " " + mClass.toString() + "." + mNameAndType.toString();
        }
    }

    public class ConstantFieldRef extends ConstantRef
    {
        private ConstantFieldRef(int             aTag,
                                 DataInputStream aIn)
                          throws IOException
        {
            super(aTag, aIn);
        }
    }

    public class ConstantMethodRef extends ConstantRef
    {
        private ConstantMethodRef(ConstantClass       aClass,
                                  ConstantNameAndType aNameAndType)
        {
            super(METHODREF, aClass, aNameAndType);
        }

        private ConstantMethodRef(int             aTag,
                                  DataInputStream aIn)
                           throws IOException
        {
            super(aTag, aIn);
        }
    }

    public class ConstantInterfaceMethodRef extends ConstantRef
    {
        private ConstantInterfaceMethodRef(int             aTag,
                                           DataInputStream aIn)
                                    throws IOException
        {
            super(aTag, aIn);
        }
    }

    public class ConstantNameAndType extends Constant
    {
        private final int    name_index;
        private final int    descriptor_index;
        private ConstantUtf8 mName = null;
        private ConstantUtf8 mDescriptor = null;

        private ConstantNameAndType(ConstantUtf8 aName,
                                    ConstantUtf8 aDescriptor)
        {
            super(NAMEANDTYPE);
            this.name_index = 0;
            this.descriptor_index = 0;
            mName = aName;
            mDescriptor = aDescriptor;
            mName.incRefCount();
            mDescriptor.incRefCount();
        }

        protected ConstantNameAndType(int             aTag,
                                      DataInputStream aIn)
                               throws IOException
        {
            super(aTag);
            this.name_index = aIn.readUnsignedShort();
            this.descriptor_index = aIn.readUnsignedShort();
        }

        protected void update()
                       throws ClassFileException
        {
            Constant constantName       = getByIndex(this.name_index);
            Constant constantDescriptor = getByIndex(this.descriptor_index);
            if ((constantName instanceof ConstantUtf8) && (constantDescriptor instanceof ConstantUtf8))
            {
                mName       = (ConstantUtf8)constantName;
                mDescriptor = (ConstantUtf8)constantDescriptor;
                mName.incRefCount();
                mDescriptor.incRefCount();
            }
            else
            {
                throw new ClassFileException("Invalid constant reference");
            }
        }

        protected void store(DataOutputStream aOut)
                      throws IOException
        {
            super.store(aOut);
            aOut.writeShort(mName.getIndex());
            aOut.writeShort(mDescriptor.getIndex());
        }

        public String toValue()
        {
            return "#" + mName.getIndex() + ":#" + mDescriptor.getIndex();
        }

        public String toString()
        {
            return mName.toString() + ":" + mDescriptor.toString();
        }
    }
}
