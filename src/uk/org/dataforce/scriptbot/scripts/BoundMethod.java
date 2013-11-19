/*
 * Copyright (c) 2006-2013 Shane Mc Cormack
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package uk.org.dataforce.scriptbot.scripts;

/**
 * Class representing a bound method call.
 */
public class BoundMethod {
    /** Object that is bound. */
    public final Object object;
    /** Method that is bound. */
    public final Object method;

    /**
     * Create a new BoundMethod
     *
     * @param object Object to bind.
     * @param method Method to bind.
     */
    public BoundMethod(final Object object, final Object method) {
        this.object = object;
        this.method = method;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (o instanceof BoundMethod) {
            final BoundMethod bm = (BoundMethod) o;
            if (this.object == null && bm.object != null) {
                return false;
            }
            if (this.method == null && bm.method != null) {
                return false;
            }
            return this.object.equals(bm.object) && this.method.equals(bm.method);
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.object != null ? this.object.hashCode() : 0);
        hash = 29 * hash + (this.method != null ? this.method.hashCode() : 0);
        return hash;
    }

}
