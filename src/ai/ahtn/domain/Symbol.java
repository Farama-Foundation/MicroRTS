/*
 * Creator: Santi Ontanon Villar
 */
/**
 * Copyright (c) 2013, Santiago Ontañón All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. Neither the name of the IIIA-CSIC nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission. THIS SOFTWARE IS PROVIDED
 * BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package ai.ahtn.domain;

import java.util.HashMap;

/**
 * The Class Symbol.
 */
public class Symbol {

    static HashMap<String, StringBuffer> sSymbolHash = new HashMap<>();

    StringBuffer mSym;

    /**
     * Instantiates a new symbol.
     */
    public Symbol(String sym) throws Exception {
        if (sym == null) {
            throw new Exception("null name in a Symbol!!!");
        }
        if (sSymbolHash.containsKey(sym)) {
            mSym = sSymbolHash.get(sym);
        } else {
            mSym = new StringBuffer(sym);
            sSymbolHash.put(sym, mSym);
        }
    }
    

    /**
     * Instantiates a new symbol.
     */
    public Symbol(Symbol sym) {
        mSym = sym.mSym;
    }


    public String get() {
        return mSym.toString();
    }

    
    public void set(String str) {
        mSym = new StringBuffer(str);
    }
    

    public boolean equals(Object o) {
        if (o instanceof String) {
            return equals((String) o);
        } else if (o instanceof StringBuffer) {
            return equals((StringBuffer) o);
        } else if (o instanceof Symbol) {
            return equals((Symbol) o);
        }
        return false;
    }

    
    public boolean equals(String str) {
        if (mSym == null) {
            return str == null;
        }
        if (str == null) {
            return false;
        }
        return (mSym.toString().equals(str));
    }
    

    public boolean equals(StringBuffer str) {
        if (mSym == null) {
            return str == null;
        }
        if (str == null) {
            return false;
        }
        return (mSym.toString().equals(str.toString()));
    }
    

    public boolean equals(Symbol sym) {
        return mSym == sym.mSym;
    }

    
    static void arrangeString(StringBuffer str) {
        int len;

        while (str.charAt(0) == ' ' || str.charAt(0) == '\n' || str.charAt(0) == '\r' || str.charAt(0) == '\t') {
            str = str.deleteCharAt(0);
        }

        len = str.length();
        while (len > 1 && (str.charAt(len - 1) == ' ' || str.charAt(len - 1) == '\n' || str.charAt(len - 1) == '\r' || str.charAt(len - 1) == '\t')) {
            str = str.deleteCharAt(len - 1);
            len--;
        }
    }

    
    public String toString() {
        return mSym.toString();
    }

    
    public int hashCode() {
        if (mSym == null) {
            return 0;
        }
        return mSym.hashCode();
    }

}
