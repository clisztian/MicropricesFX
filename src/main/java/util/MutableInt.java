
package util;

public class MutableInt implements Comparable<MutableInt> {
    int value = 1; // note that we start at 1 since we're counting
    public void increment () { ++value;      }
    public int  get ()       { return value; }

    @Override
    public int compareTo(MutableInt o) {
        return Integer.compare(get(), o.get());
    }
}