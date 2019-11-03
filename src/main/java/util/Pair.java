package util;



public class Pair<T1,T2> {
	public T1 m_a;
	public T2 m_b;
	
	public Pair(T1 a,T2 b) {
		m_a = a;
		m_b = b;
	}   
        
        public String toString() {
            return "<" + m_a + "," + m_b + ">";
        }
}
