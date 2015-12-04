package de.mhus.app.inka.ant.example;

/*
Code revised from 



Cooking with Java XP
by Eric M. Burke and Brian M. Coyner

ISBN: 0-596-00387-0
Pages: 288

<?xml version="1.0"?>
<project name="Ant Task" default="compile" basedir=".">
  <property name="dir.build" value="build"/>
  <property name="dir.dist" value="dist"/>
  <property name="dir.src" value="src"/>

  <path id="classpath.project">
    <pathelement path="${dir.build}"/>
  </path>

  <target name="compile" description="Compile all source code.">
    <javac srcdir="${dir.src}" destdir="${dir.build}">
      <classpath refid="classpath.project"/>
    </javac>
  </target>

  <target name="demoDialogBox" depends="compile">
      <taskdef name="dialogbox"
              classname="DialogBoxTask"
              classpath="${dir.build}"/>

      <dialogbox message="Are you ready?"
                 title="Important Question"
                 property="response"
                 optiontype="yes_no"/>

      <dialogbox message="You entered ${response}!"/>

      <dialogbox title="First response: ${response}">This is a dialog with a multi-line message.</dialogbox>
  </target>
</project>



-------------------------------------------------------------------
*/

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.EnumeratedAttribute;

import javax.swing.*;

public class DialogBoxTask extends Task {
    private String message;
    private String title = "Question";
    private int optionType = -1;
    private String propertyName;

    public void setTitle(String title) {
        this.title = title;
    }

    public void setProperty(String propertyName) {
        this.propertyName = propertyName;
    }

    public void setOptiontype(OptionType ot) {
        log("Calling setOptionType: " + ot.getValue(),
                Project.MSG_DEBUG);

        String value = ot.getValue();
        if ("ok".equals(value)) {
            optionType = -1;
        } else if ("ok_cancel".equals(value)) {
            optionType = JOptionPane.OK_CANCEL_OPTION;
        } else if ("yes_no".equals(value)) {
            optionType = JOptionPane.YES_NO_OPTION;
        } else {
            // only remaining possibility
            optionType = JOptionPane.YES_NO_CANCEL_OPTION;
        }
    }

    public void setMessage(String msg) {
        // ant always replaces properties for attributes
        message = msg;
    }

    public void addText(String msg) {
        if (message == null) {
            message = "";
        }
        // we must manually replace properties for nested text
        message += ProjectHelper.replaceProperties(
                getProject(), msg, getProject().getProperties());
    }

    public void execute() throws BuildException {
        validateAttributes();

        log("optionType = " + optionType, Project.MSG_DEBUG);

        if (optionType == -1) {
            JOptionPane.showMessageDialog(
                    null, // parent
                    message,
                    title,
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            int response = JOptionPane.showConfirmDialog(
                    null, // parent
                    message,
                    title,
                    optionType,
                    JOptionPane.QUESTION_MESSAGE);
            if (propertyName != null) {
                String responseText = formatResponseCode(response);
                log("Setting " + propertyName + " to " + responseText,
                        Project.MSG_VERBOSE);
                getProject().setProperty(propertyName, responseText);
            }
        }
    }

    protected void validateAttributes() {
        if (message == null) {
            throw new BuildException("Message must be specified using the "
                    + "message attribute or nested text.");
        }

        if (optionType == -1 && propertyName != null) {
            throw new BuildException(
                    "Cannot specify property unless optionType is "
                    + "'ok_cancel', 'yes_no', or 'yes_no_cancel'");
        }
    }

    public static class OptionType extends EnumeratedAttribute {
        public String[] getValues() {
            return new String[]{
                "ok",
                "ok_cancel",
                "yes_no",
                "yes_no_cancel",
            };
        }
    }

    private String formatResponseCode(int optionPaneResponse) {
        switch (optionPaneResponse) {
            // note: JOptionPane.OK_OPTION is the same as YES_OPTION
            case JOptionPane.YES_OPTION:
                return "yes";
            case JOptionPane.NO_OPTION:
                return "no";
            case JOptionPane.CANCEL_OPTION:
            case JOptionPane.CLOSED_OPTION:
                return "cancel";
            default:
                throw new BuildException("Internal error: Unknown option " +
                        "pane response: " + optionPaneResponse);
        }
    }
}
