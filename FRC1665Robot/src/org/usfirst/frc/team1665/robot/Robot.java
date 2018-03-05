/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.usfirst.frc.team1665.robot;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.properties file in the
 * project.
 */
public class Robot extends IterativeRobot {
	private static final String doNothingAuto = "Do Nothing";
	private static final String driveForwardAuto = "Drive Forward";
	private static final String driveFarAuto = "Drive Forward (far)";
	private static final String dropBoxInScaleAuto = "Drop Box In Scale";
	private String autoSelected;
	private SendableChooser<String> m_chooser = new SendableChooser<>();
	private MyRobot robot;

	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	@Override
	public void robotInit() {
		m_chooser.addDefault("Do Nothing", doNothingAuto);
		m_chooser.addObject("Drive Forward", driveForwardAuto);
		m_chooser.addObject("Drive Foward (far)", driveFarAuto);
		m_chooser.addObject("Drop Box In Scale", dropBoxInScaleAuto);
		SmartDashboard.putData("Auto choices", m_chooser);
		robot = new MyRobot();
	}

	/**
	 * This autonomous (along with the chooser code above) shows how to select
	 * between different autonomous modes using the dashboard. The sendable
	 * chooser code works with the Java SmartDashboard. If you prefer the
	 * LabVIEW Dashboard, remove all of the chooser code and uncomment the
	 * getString line to get the auto name from the text box below the Gyro
	 *
	 * <p>You can add additional auto modes by adding additional comparisons to
	 * the switch structure below with additional strings. If using the
	 * SendableChooser make sure to add them to the chooser code above as well.
	 */
	@Override
	public void autonomousInit() {
		autoSelected = m_chooser.getSelected();
//		autoSelected = SmartDashboard.getString("Auto Selector", doNothingAuto);
		System.out.println("Auto selected: " + autoSelected);
	}

	
	
	/**
	 * This function is called periodically during autonomous.
	 */
	@Override
	public void autonomousPeriodic() {	
		robot.drivePastLine(3);
		
		/*
		switch (autoSelected) {
			case doNothingAuto:
				// Put custom auto code here
				System.out.println("NOTHING");
				break;
			case driveForwardAuto:
				robot.drivePastLine(3);
				System.out.println("Drive Forward");
				break;
			case driveFarAuto:
				System.out.println("Drive Far");
//				robot.drivePastLine(5);
				break;
			case dropBoxInScaleAuto:
				System.out.println("Drop Box in Scale");
				break;
			default:
				System.out.println("Default");
				// Put default auto code here
				break;
		}
		
		*/
		
	}

	/**
	 * This function is called periodically during operator control.
	 */
	@Override
	public void teleopPeriodic() {
		robot.teleop();
	}

	/**
	 * This function is called periodically during test mode.
	 */
	@Override
	public void testPeriodic() {
	}
}
