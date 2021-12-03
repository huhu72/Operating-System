
// Java program to illustrate defining Thread
// by implements Runnable interface

  
// Here we can extends any other class
class p extends c implements Runnable {
   
   public void run()
    {
    	
        System.out.println("Running code within p thread");
        
        c.m1();
    }
}
class c {
     
    public static void main(String[] args)
    {
        p p = new p();
        Thread t1 = new Thread(p);
        t1.start(); 
        System.out.println("Main method executed by main thread");
        System.out.println("p2");
    }
    public static void m1()
    {
        System.out.println("Running cpu run code");
    }
}