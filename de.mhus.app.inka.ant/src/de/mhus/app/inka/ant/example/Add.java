package de.mhus.app.inka.ant.example;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

// Example from oreilly
/*

<?xml version="1.0"?>

<project default="main">

<taskdef name="adder" classname="de.mhus.app.inka.ant.example.Add"/>

<target name="main">
 <adder op1="23" op2="77"/>
<target>

<project>

 */
public class Add extends Task {

    private int op1;
    private int op2;
    private int sum;

    // The method executing the task
       public void execute()
           throws BuildException {
           sum = op1 + op2;
           System.out.println("The sum of the " +
               "operands is " + sum + ".");
       }

   // The setter for the "op1" attribute
       public void setOp1(int op1) {
          this.op1 = op1;
       }

   // The setter for the "op2" attribute
       public void setOp2(int op2) {
          this.op2 = op2;
       }
}