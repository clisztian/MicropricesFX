package util;

import java.util.*;


public class QuickSort {
    /** Utility classes should not have public constructors. */
    private QuickSort() {

    }

    private static final int M = 7;
    private static final int NSTACK = 64;

    public static HashMap<String, MutableInt> sort_mutable(HashMap<String, MutableInt> hm)  {

        // Create a list from elements of HashMap

        List<Map.Entry<String, MutableInt> > list = new LinkedList<Map.Entry<String, MutableInt> >(hm.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, MutableInt> >() {

            public int compare(Map.Entry<String, MutableInt> o1,  Map.Entry<String, MutableInt> o2) {
                return ((Integer)o1.getValue().get()).compareTo((Integer)o2.getValue().get());
            }
        });

        Collections.reverse(list);
        int total = 0;
        HashMap<String, MutableInt> temp = new LinkedHashMap<String, MutableInt>();
        for (Map.Entry<String, MutableInt> aa : list) {
        	total += aa.getValue().value;
            temp.put(aa.getKey(), aa.getValue());
        }

        return temp;

    }


    
    
    /**
     * Sorts the specified array into ascending numerical order.
     * @return the original index of elements after sorting in range [0, n).
     */
    public static int[] sort(int[] arr) {
        int[] order = new int[arr.length];
        for (int i = 0; i < order.length; i++) {
            order[i] = i;
        }
        sort(arr, order);
        return order;
    }

    /**
     * Besides sorting the array arr, the array brr will be also
     * rearranged as the same order of arr.
     */
    public static void sort(int[] arr, int[] brr) {
        sort(arr, brr, arr.length);
    }

    /**
     * This is an efficient implementation Quick Sort algorithm without
     * recursive. Besides sorting the first n elements of array arr, the first
     * n elements of array brr will be also rearranged as the same order of arr.
     */
    public static void sort(int[] arr, int[] brr, int n) {
        int jstack = -1;
        int l = 0;
        int[] istack = new int[NSTACK];
        int ir = n - 1;

        int i, j, k, a, b;
        for (;;) {
            if (ir - l < M) {
                for (j = l + 1; j <= ir; j++) {
                    a = arr[j];
                    b = brr[j];
                    for (i = j - 1; i >= l; i--) {
                        if (arr[i] <= a) {
                            break;
                        }
                        arr[i + 1] = arr[i];
                        brr[i + 1] = brr[i];
                    }
                    arr[i + 1] = a;
                    brr[i + 1] = b;
                }
                if (jstack < 0) {
                    break;
                }
                ir = istack[jstack--];
                l = istack[jstack--];
            } else {
                k = (l + ir) >> 1;
                Sort.swap(arr, k, l + 1);
                Sort.swap(brr, k, l + 1);
                if (arr[l] > arr[ir]) {
                    Sort.swap(arr, l, ir);
                    Sort.swap(brr, l, ir);
                }
                if (arr[l + 1] > arr[ir]) {
                    Sort.swap(arr, l + 1, ir);
                    Sort.swap(brr, l + 1, ir);
                }
                if (arr[l] > arr[l + 1]) {
                    Sort.swap(arr, l, l + 1);
                    Sort.swap(brr, l, l + 1);
                }
                i = l + 1;
                j = ir;
                a = arr[l + 1];
                b = brr[l + 1];
                for (;;) {
                    do {
                        i++;
                    } while (arr[i] < a);
                    do {
                        j--;
                    } while (arr[j] > a);
                    if (j < i) {
                        break;
                    }
                    Sort.swap(arr, i, j);
                    Sort.swap(brr, i, j);
                }
                arr[l + 1] = arr[j];
                arr[j] = a;
                brr[l + 1] = brr[j];
                brr[j] = b;
                jstack += 2;

                if (jstack >= NSTACK) {
                    throw new IllegalStateException("NSTACK too small in sort.");
                }

                if (ir - i + 1 >= j - l) {
                    istack[jstack] = ir;
                    istack[jstack - 1] = i;
                    ir = j - 1;
                } else {
                    istack[jstack] = j - 1;
                    istack[jstack - 1] = l;
                    l = i;
                }
            }
        }
    }

    /**
     * Besides sorting the array arr, the array brr will be also
     * rearranged as the same order of arr.
     */
    public static void sort(int[] arr, double[] brr) {
        sort(arr, brr, arr.length);
    }

    /**
     * This is an efficient implementation Quick Sort algorithm without
     * recursive. Besides sorting the first n elements of array arr, the first
     * n elements of array brr will be also rearranged as the same order of arr.
     */
    public static void sort(int[] arr, double[] brr, int n) {
        int jstack = -1;
        int l = 0;
        int[] istack = new int[NSTACK];
        int ir = n - 1;

        int i, j, k, a;
        double b;
        for (;;) {
            if (ir - l < M) {
                for (j = l + 1; j <= ir; j++) {
                    a = arr[j];
                    b = brr[j];
                    for (i = j - 1; i >= l; i--) {
                        if (arr[i] <= a) {
                            break;
                        }
                        arr[i + 1] = arr[i];
                        brr[i + 1] = brr[i];
                    }
                    arr[i + 1] = a;
                    brr[i + 1] = b;
                }
                if (jstack < 0) {
                    break;
                }
                ir = istack[jstack--];
                l = istack[jstack--];
            } else {
                k = (l + ir) >> 1;
                Sort.swap(arr, k, l + 1);
                Sort.swap(brr, k, l + 1);
                if (arr[l] > arr[ir]) {
                    Sort.swap(arr, l, ir);
                    Sort.swap(brr, l, ir);
                }
                if (arr[l + 1] > arr[ir]) {
                    Sort.swap(arr, l + 1, ir);
                    Sort.swap(brr, l + 1, ir);
                }
                if (arr[l] > arr[l + 1]) {
                    Sort.swap(arr, l, l + 1);
                    Sort.swap(brr, l, l + 1);
                }
                i = l + 1;
                j = ir;
                a = arr[l + 1];
                b = brr[l + 1];
                for (;;) {
                    do {
                        i++;
                    } while (arr[i] < a);
                    do {
                        j--;
                    } while (arr[j] > a);
                    if (j < i) {
                        break;
                    }
                    Sort.swap(arr, i, j);
                    Sort.swap(brr, i, j);
                }
                arr[l + 1] = arr[j];
                arr[j] = a;
                brr[l + 1] = brr[j];
                brr[j] = b;
                jstack += 2;

                if (jstack >= NSTACK) {
                    throw new IllegalStateException("NSTACK too small in sort.");
                }

                if (ir - i + 1 >= j - l) {
                    istack[jstack] = ir;
                    istack[jstack - 1] = i;
                    ir = j - 1;
                } else {
                    istack[jstack] = j - 1;
                    istack[jstack - 1] = l;
                    l = i;
                }
            }
        }
    }

    /**
     * Besides sorting the array arr, the array brr will be also
     * rearranged as the same order of arr.
     */
    public static void sort(int[] arr, Object[] brr) {
        sort(arr, brr, arr.length);
    }

    /**
     * This is an efficient implementation Quick Sort algorithm without
     * recursive. Besides sorting the first n elements of array arr, the first
     * n elements of array brr will be also rearranged as the same order of arr.
     */
    public static void sort(int[] arr, Object[] brr, int n) {
        int jstack = -1;
        int l = 0;
        int[] istack = new int[NSTACK];
        int ir = n - 1;

        int i, j, k, a;
        Object b;
        for (;;) {
            if (ir - l < M) {
                for (j = l + 1; j <= ir; j++) {
                    a = arr[j];
                    b = brr[j];
                    for (i = j - 1; i >= l; i--) {
                        if (arr[i] <= a) {
                            break;
                        }
                        arr[i + 1] = arr[i];
                        brr[i + 1] = brr[i];
                    }
                    arr[i + 1] = a;
                    brr[i + 1] = b;
                }
                if (jstack < 0) {
                    break;
                }
                ir = istack[jstack--];
                l = istack[jstack--];
            } else {
                k = (l + ir) >> 1;
                Sort.swap(arr, k, l + 1);
                Sort.swap(brr, k, l + 1);
                if (arr[l] > arr[ir]) {
                    Sort.swap(arr, l, ir);
                    Sort.swap(brr, l, ir);
                }
                if (arr[l + 1] > arr[ir]) {
                    Sort.swap(arr, l + 1, ir);
                    Sort.swap(brr, l + 1, ir);
                }
                if (arr[l] > arr[l + 1]) {
                    Sort.swap(arr, l, l + 1);
                    Sort.swap(brr, l, l + 1);
                }
                i = l + 1;
                j = ir;
                a = arr[l + 1];
                b = brr[l + 1];
                for (;;) {
                    do {
                        i++;
                    } while (arr[i] < a);
                    do {
                        j--;
                    } while (arr[j] > a);
                    if (j < i) {
                        break;
                    }
                    Sort.swap(arr, i, j);
                    Sort.swap(brr, i, j);
                }
                arr[l + 1] = arr[j];
                arr[j] = a;
                brr[l + 1] = brr[j];
                brr[j] = b;
                jstack += 2;

                if (jstack >= NSTACK) {
                    throw new IllegalStateException("NSTACK too small in sort.");
                }

                if (ir - i + 1 >= j - l) {
                    istack[jstack] = ir;
                    istack[jstack - 1] = i;
                    ir = j - 1;
                } else {
                    istack[jstack] = j - 1;
                    istack[jstack - 1] = l;
                    l = i;
                }
            }
        }
    }

    /**
     * Sorts the specified array into ascending numerical order.
     * @return the original index of elements after sorting in range [0, n).
     */
    public static int[] sort(float[] arr) {
        int[] order = new int[arr.length];
        for (int i = 0; i < order.length; i++) {
            order[i] = i;
        }
        sort(arr, order);
        return order;
    }

    /**
     * Besides sorting the array arr, the array brr will be also
     * rearranged as the same order of arr.
     */
    public static void sort(float[] arr, int[] brr) {
        sort(arr, brr, arr.length);
    }

    /**
     * This is an efficient implementation Quick Sort algorithm without
     * recursive. Besides sorting the first n elements of array arr, the first
     * n elements of array brr will be also rearranged as the same order of arr.
     */
    public static void sort(float[] arr, int[] brr, int n) {
        int jstack = -1;
        int l = 0;
        int[] istack = new int[NSTACK];
        int ir = n - 1;

        int i, j, k;
        float a;
        int b;
        for (;;) {
            if (ir - l < M) {
                for (j = l + 1; j <= ir; j++) {
                    a = arr[j];
                    b = brr[j];
                    for (i = j - 1; i >= l; i--) {
                        if (arr[i] <= a) {
                            break;
                        }
                        arr[i + 1] = arr[i];
                        brr[i + 1] = brr[i];
                    }
                    arr[i + 1] = a;
                    brr[i + 1] = b;
                }
                if (jstack < 0) {
                    break;
                }
                ir = istack[jstack--];
                l = istack[jstack--];
            } else {
                k = (l + ir) >> 1;
                Sort.swap(arr, k, l + 1);
                Sort.swap(brr, k, l + 1);
                if (arr[l] > arr[ir]) {
                    Sort.swap(arr, l, ir);
                    Sort.swap(brr, l, ir);
                }
                if (arr[l + 1] > arr[ir]) {
                    Sort.swap(arr, l + 1, ir);
                    Sort.swap(brr, l + 1, ir);
                }
                if (arr[l] > arr[l + 1]) {
                    Sort.swap(arr, l, l + 1);
                    Sort.swap(brr, l, l + 1);
                }
                i = l + 1;
                j = ir;
                a = arr[l + 1];
                b = brr[l + 1];
                for (;;) {
                    do {
                        i++;
                    } while (arr[i] < a);
                    do {
                        j--;
                    } while (arr[j] > a);
                    if (j < i) {
                        break;
                    }
                    Sort.swap(arr, i, j);
                    Sort.swap(brr, i, j);
                }
                arr[l + 1] = arr[j];
                arr[j] = a;
                brr[l + 1] = brr[j];
                brr[j] = b;
                jstack += 2;

                if (jstack >= NSTACK) {
                    throw new IllegalStateException("NSTACK too small in sort.");
                }

                if (ir - i + 1 >= j - l) {
                    istack[jstack] = ir;
                    istack[jstack - 1] = i;
                    ir = j - 1;
                } else {
                    istack[jstack] = j - 1;
                    istack[jstack - 1] = l;
                    l = i;
                }
            }
        }
    }

    /**
     * Besides sorting the array arr, the array brr will be also
     * rearranged as the same order of arr.
     */
    public static void sort(float[] arr, float[] brr) {
        sort(arr, brr, arr.length);
    }

    /**
     * This is an efficient implementation Quick Sort algorithm without
     * recursive. Besides sorting the first n elements of array arr, the first
     * n elements of array brr will be also rearranged as the same order of arr.
     */
    public static void sort(float[] arr, float[] brr, int n) {
        int jstack = -1;
        int l = 0;
        int[] istack = new int[NSTACK];
        int ir = n - 1;

        int i, j, k;
        float a, b;
        for (;;) {
            if (ir - l < M) {
                for (j = l + 1; j <= ir; j++) {
                    a = arr[j];
                    b = brr[j];
                    for (i = j - 1; i >= l; i--) {
                        if (arr[i] <= a) {
                            break;
                        }
                        arr[i + 1] = arr[i];
                        brr[i + 1] = brr[i];
                    }
                    arr[i + 1] = a;
                    brr[i + 1] = b;
                }
                if (jstack < 0) {
                    break;
                }
                ir = istack[jstack--];
                l = istack[jstack--];
            } else {
                k = (l + ir) >> 1;
                Sort.swap(arr, k, l + 1);
                Sort.swap(brr, k, l + 1);
                if (arr[l] > arr[ir]) {
                    Sort.swap(arr, l, ir);
                    Sort.swap(brr, l, ir);
                }
                if (arr[l + 1] > arr[ir]) {
                    Sort.swap(arr, l + 1, ir);
                    Sort.swap(brr, l + 1, ir);
                }
                if (arr[l] > arr[l + 1]) {
                    Sort.swap(arr, l, l + 1);
                    Sort.swap(brr, l, l + 1);
                }
                i = l + 1;
                j = ir;
                a = arr[l + 1];
                b = brr[l + 1];
                for (;;) {
                    do {
                        i++;
                    } while (arr[i] < a);
                    do {
                        j--;
                    } while (arr[j] > a);
                    if (j < i) {
                        break;
                    }
                    Sort.swap(arr, i, j);
                    Sort.swap(brr, i, j);
                }
                arr[l + 1] = arr[j];
                arr[j] = a;
                brr[l + 1] = brr[j];
                brr[j] = b;
                jstack += 2;

                if (jstack >= NSTACK) {
                    throw new IllegalStateException("NSTACK too small in sort.");
                }

                if (ir - i + 1 >= j - l) {
                    istack[jstack] = ir;
                    istack[jstack - 1] = i;
                    ir = j - 1;
                } else {
                    istack[jstack] = j - 1;
                    istack[jstack - 1] = l;
                    l = i;
                }
            }
        }
    }

    /**
     * Besides sorting the array arr, the array brr will be also
     * rearranged as the same order of arr.
     */
    public static void sort(float[] arr, Object[] brr) {
        sort(arr, brr, arr.length);
    }

    /**
     * This is an efficient implementation Quick Sort algorithm without
     * recursive. Besides sorting the first n elements of array arr, the first
     * n elements of array brr will be also rearranged as the same order of arr.
     */
    public static void sort(float[] arr, Object[] brr, int n) {
        int jstack = -1;
        int l = 0;
        int[] istack = new int[NSTACK];
        int ir = n - 1;

        int i, j, k;
        float a;
        Object b;
        for (;;) {
            if (ir - l < M) {
                for (j = l + 1; j <= ir; j++) {
                    a = arr[j];
                    b = brr[j];
                    for (i = j - 1; i >= l; i--) {
                        if (arr[i] <= a) {
                            break;
                        }
                        arr[i + 1] = arr[i];
                        brr[i + 1] = brr[i];
                    }
                    arr[i + 1] = a;
                    brr[i + 1] = b;
                }
                if (jstack < 0) {
                    break;
                }
                ir = istack[jstack--];
                l = istack[jstack--];
            } else {
                k = (l + ir) >> 1;
                Sort.swap(arr, k, l + 1);
                Sort.swap(brr, k, l + 1);
                if (arr[l] > arr[ir]) {
                    Sort.swap(arr, l, ir);
                    Sort.swap(brr, l, ir);
                }
                if (arr[l + 1] > arr[ir]) {
                    Sort.swap(arr, l + 1, ir);
                    Sort.swap(brr, l + 1, ir);
                }
                if (arr[l] > arr[l + 1]) {
                    Sort.swap(arr, l, l + 1);
                    Sort.swap(brr, l, l + 1);
                }
                i = l + 1;
                j = ir;
                a = arr[l + 1];
                b = brr[l + 1];
                for (;;) {
                    do {
                        i++;
                    } while (arr[i] < a);
                    do {
                        j--;
                    } while (arr[j] > a);
                    if (j < i) {
                        break;
                    }
                    Sort.swap(arr, i, j);
                    Sort.swap(brr, i, j);
                }
                arr[l + 1] = arr[j];
                arr[j] = a;
                brr[l + 1] = brr[j];
                brr[j] = b;
                jstack += 2;

                if (jstack >= NSTACK) {
                    throw new IllegalStateException("NSTACK too small in sort.");
                }

                if (ir - i + 1 >= j - l) {
                    istack[jstack] = ir;
                    istack[jstack - 1] = i;
                    ir = j - 1;
                } else {
                    istack[jstack] = j - 1;
                    istack[jstack - 1] = l;
                    l = i;
                }
            }
        }
    }

    /**
     * Sorts the specified array into ascending numerical order.
     * @return the original index of elements after sorting in range [0, n).
     */
    public static int[] sort(double[] arr) {
        int[] order = new int[arr.length];
        for (int i = 0; i < order.length; i++) {
            order[i] = i;
        }
        sort(arr, order);
        return order;
    }

    /**
     * Besides sorting the array arr, the array brr will be also
     * rearranged as the same order of arr.
     */
    public static void sort(double[] arr, int[] brr) {
        sort(arr, brr, arr.length);
    }

    /**
     * This is an efficient implementation Quick Sort algorithm without
     * recursive. Besides sorting the first n elements of array arr, the first
     * n elements of array brr will be also rearranged as the same order of arr.
     */
    public static void sort(double[] arr, int[] brr, int n) {
        int jstack = -1;
        int l = 0;
        int[] istack = new int[NSTACK];
        int ir = n - 1;

        int i, j, k;
        double a;
        int b;
        for (;;) {
            if (ir - l < M) {
                for (j = l + 1; j <= ir; j++) {
                    a = arr[j];
                    b = brr[j];
                    for (i = j - 1; i >= l; i--) {
                        if (arr[i] <= a) {
                            break;
                        }
                        arr[i + 1] = arr[i];
                        brr[i + 1] = brr[i];
                    }
                    arr[i + 1] = a;
                    brr[i + 1] = b;
                }
                if (jstack < 0) {
                    break;
                }
                ir = istack[jstack--];
                l = istack[jstack--];
            } else {
                k = (l + ir) >> 1;
                Sort.swap(arr, k, l + 1);
                Sort.swap(brr, k, l + 1);
                if (arr[l] > arr[ir]) {
                    Sort.swap(arr, l, ir);
                    Sort.swap(brr, l, ir);
                }
                if (arr[l + 1] > arr[ir]) {
                    Sort.swap(arr, l + 1, ir);
                    Sort.swap(brr, l + 1, ir);
                }
                if (arr[l] > arr[l + 1]) {
                    Sort.swap(arr, l, l + 1);
                    Sort.swap(brr, l, l + 1);
                }
                i = l + 1;
                j = ir;
                a = arr[l + 1];
                b = brr[l + 1];
                for (;;) {
                    do {
                        i++;
                    } while (arr[i] < a);
                    do {
                        j--;
                    } while (arr[j] > a);
                    if (j < i) {
                        break;
                    }
                    Sort.swap(arr, i, j);
                    Sort.swap(brr, i, j);
                }
                arr[l + 1] = arr[j];
                arr[j] = a;
                brr[l + 1] = brr[j];
                brr[j] = b;
                jstack += 2;

                if (jstack >= NSTACK) {
                    throw new IllegalStateException("NSTACK too small in sort.");
                }

                if (ir - i + 1 >= j - l) {
                    istack[jstack] = ir;
                    istack[jstack - 1] = i;
                    ir = j - 1;
                } else {
                    istack[jstack] = j - 1;
                    istack[jstack - 1] = l;
                    l = i;
                }
            }
        }
    }

    /**
     * This is an efficient implementation Quick Sort algorithm without
     * recursive. Besides sorting the array arr, the array brr will be also
     * rearranged as the same order of arr.
     */
    public static void sort(double[] arr, double[] brr) {
        sort(arr, brr, arr.length);
    }

    /**
     * This is an efficient implementation Quick Sort algorithm without
     * recursive. Besides sorting the first n elements of array arr, the first
     * n elements of array brr will be also rearranged as the same order of arr.
     */
    public static void sort(double[] arr, double[] brr, int n) {
        int jstack = -1;
        int l = 0;
        int[] istack = new int[NSTACK];
        int ir = n - 1;

        int i, j, k;
        double a, b;
        for (;;) {
            if (ir - l < M) {
                for (j = l + 1; j <= ir; j++) {
                    a = arr[j];
                    b = brr[j];
                    for (i = j - 1; i >= l; i--) {
                        if (arr[i] <= a) {
                            break;
                        }
                        arr[i + 1] = arr[i];
                        brr[i + 1] = brr[i];
                    }
                    arr[i + 1] = a;
                    brr[i + 1] = b;
                }
                if (jstack < 0) {
                    break;
                }
                ir = istack[jstack--];
                l = istack[jstack--];
            } else {
                k = (l + ir) >> 1;
                Sort.swap(arr, k, l + 1);
                Sort.swap(brr, k, l + 1);
                if (arr[l] > arr[ir]) {
                    Sort.swap(arr, l, ir);
                    Sort.swap(brr, l, ir);
                }
                if (arr[l + 1] > arr[ir]) {
                    Sort.swap(arr, l + 1, ir);
                    Sort.swap(brr, l + 1, ir);
                }
                if (arr[l] > arr[l + 1]) {
                    Sort.swap(arr, l, l + 1);
                    Sort.swap(brr, l, l + 1);
                }
                i = l + 1;
                j = ir;
                a = arr[l + 1];
                b = brr[l + 1];
                for (;;) {
                    do {
                        i++;
                    } while (arr[i] < a);
                    do {
                        j--;
                    } while (arr[j] > a);
                    if (j < i) {
                        break;
                    }
                    Sort.swap(arr, i, j);
                    Sort.swap(brr, i, j);
                }
                arr[l + 1] = arr[j];
                arr[j] = a;
                brr[l + 1] = brr[j];
                brr[j] = b;
                jstack += 2;

                if (jstack >= NSTACK) {
                    throw new IllegalStateException("NSTACK too small in sort.");
                }

                if (ir - i + 1 >= j - l) {
                    istack[jstack] = ir;
                    istack[jstack - 1] = i;
                    ir = j - 1;
                } else {
                    istack[jstack] = j - 1;
                    istack[jstack - 1] = l;
                    l = i;
                }
            }
        }
    }

    /**
     * Besides sorting the array arr, the array brr will be also
     * rearranged as the same order of arr.
     */
    public static void sort(double[] arr, Object[] brr) {
        sort(arr, brr, arr.length);
    }

    /**
     * This is an efficient implementation Quick Sort algorithm without
     * recursive. Besides sorting the first n elements of array arr, the first
     * n elements of array brr will be also rearranged as the same order of arr.
     */
    public static void sort(double[] arr, Object[] brr, int n) {
        int jstack = -1;
        int l = 0;
        int[] istack = new int[NSTACK];
        int ir = n - 1;

        int i, j, k;
        double a;
        Object b;
        for (;;) {
            if (ir - l < M) {
                for (j = l + 1; j <= ir; j++) {
                    a = arr[j];
                    b = brr[j];
                    for (i = j - 1; i >= l; i--) {
                        if (arr[i] <= a) {
                            break;
                        }
                        arr[i + 1] = arr[i];
                        brr[i + 1] = brr[i];
                    }
                    arr[i + 1] = a;
                    brr[i + 1] = b;
                }
                if (jstack < 0) {
                    break;
                }
                ir = istack[jstack--];
                l = istack[jstack--];
            } else {
                k = (l + ir) >> 1;
                Sort.swap(arr, k, l + 1);
                Sort.swap(brr, k, l + 1);
                if (arr[l] > arr[ir]) {
                    Sort.swap(arr, l, ir);
                    Sort.swap(brr, l, ir);
                }
                if (arr[l + 1] > arr[ir]) {
                    Sort.swap(arr, l + 1, ir);
                    Sort.swap(brr, l + 1, ir);
                }
                if (arr[l] > arr[l + 1]) {
                    Sort.swap(arr, l, l + 1);
                    Sort.swap(brr, l, l + 1);
                }
                i = l + 1;
                j = ir;
                a = arr[l + 1];
                b = brr[l + 1];
                for (;;) {
                    do {
                        i++;
                    } while (arr[i] < a);
                    do {
                        j--;
                    } while (arr[j] > a);
                    if (j < i) {
                        break;
                    }
                    Sort.swap(arr, i, j);
                    Sort.swap(brr, i, j);
                }
                arr[l + 1] = arr[j];
                arr[j] = a;
                brr[l + 1] = brr[j];
                brr[j] = b;
                jstack += 2;

                if (jstack >= NSTACK) {
                    throw new IllegalStateException("NSTACK too small in sort.");
                }

                if (ir - i + 1 >= j - l) {
                    istack[jstack] = ir;
                    istack[jstack - 1] = i;
                    ir = j - 1;
                } else {
                    istack[jstack] = j - 1;
                    istack[jstack - 1] = l;
                    l = i;
                }
            }
        }
    }

    /**
     * Sorts the specified array into ascending order.
     * @return the original index of elements after sorting in range [0, n).
     */
    public static <T extends Comparable<? super T>>  int[] sort(T[] arr) {
        int[] order = new int[arr.length];
        for (int i = 0; i < order.length; i++) {
            order[i] = i;
        }
        sort(arr, order);
        return order;
    }

    /**
     * Besides sorting the array arr, the array brr will be also
     * rearranged as the same order of arr.
     */
    public static <T extends Comparable<? super T>>  void sort(T[] arr, int[] brr) {
        sort(arr, brr, arr.length);
    }

    /**
     * This is an efficient implementation Quick Sort algorithm without
     * recursive. Besides sorting the first n elements of array arr, the first
     * n elements of array brr will be also rearranged as the same order of arr.
     */
    public static <T extends Comparable<? super T>>  void sort(T[] arr, int[] brr, int n) {
        int jstack = -1;
        int l = 0;
        int[] istack = new int[NSTACK];
        int ir = n - 1;

        int i, j, k;
        T a;
        int b;
        for (;;) {
            if (ir - l < M) {
                for (j = l + 1; j <= ir; j++) {
                    a = arr[j];
                    b = brr[j];
                    for (i = j - 1; i >= l; i--) {
                        if (arr[i].compareTo(a) <= 0) {
                            break;
                        }
                        arr[i + 1] = arr[i];
                        brr[i + 1] = brr[i];
                    }
                    arr[i + 1] = a;
                    brr[i + 1] = b;
                }
                if (jstack < 0) {
                    break;
                }
                ir = istack[jstack--];
                l = istack[jstack--];
            } else {
                k = (l + ir) >> 1;
                Sort.swap(arr, k, l + 1);
                Sort.swap(brr, k, l + 1);
                if (arr[l].compareTo(arr[ir]) > 0) {
                    Sort.swap(arr, l, ir);
                    Sort.swap(brr, l, ir);
                }
                if (arr[l + 1].compareTo(arr[ir]) > 0) {
                    Sort.swap(arr, l + 1, ir);
                    Sort.swap(brr, l + 1, ir);
                }
                if (arr[l].compareTo(arr[l + 1]) > 0) {
                    Sort.swap(arr, l, l + 1);
                    Sort.swap(brr, l, l + 1);
                }
                i = l + 1;
                j = ir;
                a = arr[l + 1];
                b = brr[l + 1];
                for (;;) {
                    do {
                        i++;
                    } while (arr[i].compareTo(a) < 0);
                    do {
                        j--;
                    } while (arr[j].compareTo(a) > 0);
                    if (j < i) {
                        break;
                    }
                    Sort.swap(arr, i, j);
                    Sort.swap(brr, i, j);
                }
                arr[l + 1] = arr[j];
                arr[j] = a;
                brr[l + 1] = brr[j];
                brr[j] = b;
                jstack += 2;

                if (jstack >= NSTACK) {
                    throw new IllegalStateException("NSTACK too small in sort.");
                }

                if (ir - i + 1 >= j - l) {
                    istack[jstack] = ir;
                    istack[jstack - 1] = i;
                    ir = j - 1;
                } else {
                    istack[jstack] = j - 1;
                    istack[jstack - 1] = l;
                    l = i;
                }
            }
        }
    }

    /**
     * This is an efficient implementation Quick Sort algorithm without
     * recursive. Besides sorting the first n elements of array arr, the first
     * n elements of array brr will be also rearranged as the same order of arr.
     */
    public static <T>  void sort(T[] arr, int[] brr, int n, Comparator<T> comparator) {
        int jstack = -1;
        int l = 0;
        int[] istack = new int[NSTACK];
        int ir = n - 1;

        int i, j, k;
        T a;
        int b;
        for (;;) {
            if (ir - l < M) {
                for (j = l + 1; j <= ir; j++) {
                    a = arr[j];
                    b = brr[j];
                    for (i = j - 1; i >= l; i--) {
                        if (comparator.compare(arr[i], a) <= 0) {
                            break;
                        }
                        arr[i + 1] = arr[i];
                        brr[i + 1] = brr[i];
                    }
                    arr[i + 1] = a;
                    brr[i + 1] = b;
                }
                if (jstack < 0) {
                    break;
                }
                ir = istack[jstack--];
                l = istack[jstack--];
            } else {
                k = (l + ir) >> 1;
                Sort.swap(arr, k, l + 1);
                Sort.swap(brr, k, l + 1);
                if (comparator.compare(arr[l], arr[ir]) > 0) {
                    Sort.swap(arr, l, ir);
                    Sort.swap(brr, l, ir);
                }
                if (comparator.compare(arr[l + 1], arr[ir]) > 0) {
                    Sort.swap(arr, l + 1, ir);
                    Sort.swap(brr, l + 1, ir);
                }
                if (comparator.compare(arr[l], arr[l + 1]) > 0) {
                    Sort.swap(arr, l, l + 1);
                    Sort.swap(brr, l, l + 1);
                }
                i = l + 1;
                j = ir;
                a = arr[l + 1];
                b = brr[l + 1];
                for (;;) {
                    do {
                        i++;
                    } while (comparator.compare(arr[i], a) < 0);
                    do {
                        j--;
                    } while (comparator.compare(arr[j], a) > 0);
                    if (j < i) {
                        break;
                    }
                    Sort.swap(arr, i, j);
                    Sort.swap(brr, i, j);
                }
                arr[l + 1] = arr[j];
                arr[j] = a;
                brr[l + 1] = brr[j];
                brr[j] = b;
                jstack += 2;

                if (jstack >= NSTACK) {
                    throw new IllegalStateException("NSTACK too small in sort.");
                }

                if (ir - i + 1 >= j - l) {
                    istack[jstack] = ir;
                    istack[jstack - 1] = i;
                    ir = j - 1;
                } else {
                    istack[jstack] = j - 1;
                    istack[jstack - 1] = l;
                    l = i;
                }
            }
        }
    }

    /**
     * Besides sorting the array arr, the array brr will be also
     * rearranged as the same order of arr.
     */
    public static <T extends Comparable<? super T>>  void sort(T[] arr, Object[] brr) {
        sort(arr, brr, arr.length);
    }

    /**
     * This is an efficient implementation Quick Sort algorithm without
     * recursive. Besides sorting the first n elements of array arr, the first
     * n elements of array brr will be also rearranged as the same order of arr.
     */
    public static <T extends Comparable<? super T>>  void sort(T[] arr, Object[] brr, int n) {
        int jstack = -1;
        int l = 0;
        int[] istack = new int[NSTACK];
        int ir = n - 1;

        int i, j, k;
        T a;
        Object b;
        for (;;) {
            if (ir - l < M) {
                for (j = l + 1; j <= ir; j++) {
                    a = arr[j];
                    b = brr[j];
                    for (i = j - 1; i >= l; i--) {
                        if (arr[i].compareTo(a) <= 0) {
                            break;
                        }
                        arr[i + 1] = arr[i];
                        brr[i + 1] = brr[i];
                    }
                    arr[i + 1] = a;
                    brr[i + 1] = b;
                }
                if (jstack < 0) {
                    break;
                }
                ir = istack[jstack--];
                l = istack[jstack--];
            } else {
                k = (l + ir) >> 1;
                Sort.swap(arr, k, l + 1);
                Sort.swap(brr, k, l + 1);
                if (arr[l].compareTo(arr[ir]) > 0) {
                    Sort.swap(arr, l, ir);
                    Sort.swap(brr, l, ir);
                }
                if (arr[l + 1].compareTo(arr[ir]) > 0) {
                    Sort.swap(arr, l + 1, ir);
                    Sort.swap(brr, l + 1, ir);
                }
                if (arr[l].compareTo(arr[l + 1]) > 0) {
                    Sort.swap(arr, l, l + 1);
                    Sort.swap(brr, l, l + 1);
                }
                i = l + 1;
                j = ir;
                a = arr[l + 1];
                b = brr[l + 1];
                for (;;) {
                    do {
                        i++;
                    } while (arr[i].compareTo(a) < 0);
                    do {
                        j--;
                    } while (arr[j].compareTo(a) > 0);
                    if (j < i) {
                        break;
                    }
                    Sort.swap(arr, i, j);
                    Sort.swap(brr, i, j);
                }
                arr[l + 1] = arr[j];
                arr[j] = a;
                brr[l + 1] = brr[j];
                brr[j] = b;
                jstack += 2;

                if (jstack >= NSTACK) {
                    throw new IllegalStateException("NSTACK too small in sort.");
                }

                if (ir - i + 1 >= j - l) {
                    istack[jstack] = ir;
                    istack[jstack - 1] = i;
                    ir = j - 1;
                } else {
                    istack[jstack] = j - 1;
                    istack[jstack - 1] = l;
                    l = i;
                }
            }
        }
    }
}