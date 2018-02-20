package org.usfirst.frc.team1665.robot;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.VictorSP;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Encoder;


public class MyRobot {
	// Motor pin declarations
	final int leftDrivePin1 = 1;
	final int leftDrivePin2 = 2;
	final int rightDrivePin1 = 3;
	final int rightDrivePin2 = 4;
	final int intake1Pin = 5;
	final int intake2Pin = 6;
	final int climberPin = 7;
	final int elevatorPin = 8;
	
	// Joystick ports
	final int driverJoystickPort = 0;
	final int operatorJoystickPort = 1;
	
	// Joysticks
	Joystick driverJoystick;
	Joystick operatorJoystick;
	
	// Motor controllers
	VictorSP leftDrive1;
	VictorSP leftDrive2;
	VictorSP rightDrive1;
	VictorSP rightDrive2;
	Spark intake1;
	Spark intake2;
	Talon climber;
	Talon elevator;
	
	// Pneumatics
	Compressor compressor;
	Solenoid climberLock;
	Solenoid bar;
	Solenoid gripper;
	
	// Sensors
	Encoder elevatorEncoder;
	
	// Helper variables
	boolean gripperActionPrevious = false;
	boolean previousAdjustmentMade = false;
	double eleSetpoint = 0.0;
	
	/**
	 * Initialization, sets up all motors, pneumatics,
	 * joysticks, initial positions, etc.
	 */
	public MyRobot() {
		// Joysticks
		driverJoystick = new Joystick(driverJoystickPort);
		operatorJoystick = new Joystick(operatorJoystickPort);
		
		// Base motors initialization
		leftDrive1 = new VictorSP(leftDrivePin1);
		leftDrive2 = new VictorSP(leftDrivePin2);
		rightDrive1 = new VictorSP(rightDrivePin1);
		rightDrive2 = new VictorSP(rightDrivePin2);
		
		// Intake motor initialization
		intake1 = new Spark(intake1Pin);
		intake2 = new Spark(intake2Pin);
		
		// Climber and elevator motor initialization
		climber = new Talon(climberPin);
		elevator = new Talon(elevatorPin);
		
		// Invert one intake motor so positive is inwards
		intake2.setInverted(true);
		
		// Invert the left side so positive is forward
		rightDrive1.setInverted(true);
		rightDrive2.setInverted(true);
		
		// Compressor and pneumatics
		compressor = new Compressor(0);
		climberLock = new Solenoid(1);
		bar = new Solenoid(3);
		gripper = new Solenoid(5);
		
		Solenoid ds1 = new Solenoid(2);
		Solenoid ds2 = new Solenoid(4);
		Solenoid ds3 = new Solenoid(6);
		
		ds1.set(false);
		ds2.set(false);
		ds3.set(false);
		
		// Pneumatics initial positions and turn on compressor
		compressor.setClosedLoopControl(true);
		climberLock.set(false);
		bar.set(false);
		gripper.set(false);
		
		// Encoder setup
		elevatorEncoder = new Encoder(0,1,false,Encoder.EncodingType.k4X);
		elevatorEncoder.reset();
	}
	
	/**
	 * Teleop control
	 */
	public void teleop() {
		System.out.println("A");
		baseDriveControl();
	}
	
	/**
	 * Tank drive during teleop
	 */
	public void baseDriveControl() {
		// Get joystick input
		double leftPower = driverJoystick.getRawAxis(5);
		double rightPower = driverJoystick.getRawAxis(1);
		
		// Set motor speed
		setLeftDrive(leftPower);
		setRightDrive(rightPower);
	}
	
	/**
	 * Controls the intake, gripper tension during teleop
	 */
	public void intakeControl() {	
		boolean intake = driverJoystick.getRawAxis(2) > 0.5;
		boolean outtake = driverJoystick.getRawAxis(3) > 0.5;
		boolean gripperAction = driverJoystick.getRawButton(2);
		
		double intakeSpeed = 0.75;
		double outtakeSpeed = 0.4;
		
		if (intake) {
			intake1.set(intakeSpeed);
			intake2.set(intakeSpeed);
		} else if (outtake) {
			intake1.set(-1 * outtakeSpeed);
			intake2.set(-1 * outtakeSpeed);
		} else {
			intake1.set(0);
			intake2.set(0);
		}
		
		if (gripperAction && !gripperActionPrevious) {
			gripper.set(!gripper.get());
		}
		
		gripperActionPrevious = gripperAction;
	}

	/**
	 * Controls the climber and latches during teleop
	 */
	public void climberControl() {
		boolean latch = driverJoystick.getRawButton(1);
		boolean climbUp = operatorJoystick.getRawButton(4);
		boolean climbDown = operatorJoystick.getRawButton(5);
		boolean releaseArmBar = driverJoystick.getRawButton(0);
		
		if (climbUp) {
			climber.set(1);
		} else if (climbDown) {
			climber.set(-0.5);
		} else {
			climber.set(0);
		}
		
		if (latch) {
			climberLock.set(true);
		}
		
		if (releaseArmBar) {
			bar.set(true);
		}
	}
	
	public void elevatorControl() {
		double pv = elevatorEncoder.getDistance();
		System.out.println(pv);
		
		// Preset heights
		if (operatorJoystick.getRawButton(0))
			eleSetpoint = 10;
		else if (operatorJoystick.getRawButton(1))
			eleSetpoint = 50;
		else if (operatorJoystick.getRawButton(3))
			eleSetpoint = 100;
		
		// Adjustments
		if (operatorJoystick.getRawAxis(3) > 0.5 && !previousAdjustmentMade) {
			eleSetpoint += 10;
			previousAdjustmentMade = true;
		} else if (operatorJoystick.getRawAxis(2) > 0.5 && !previousAdjustmentMade) {
			eleSetpoint -= 10;
			previousAdjustmentMade = true;
		} else if (operatorJoystick.getRawAxis(3) < 0.5 && operatorJoystick.getRawAxis(2) < 0.5)
			previousAdjustmentMade = false;
			
		
		double error = pv - eleSetpoint;
		double motorSpeed = error * 0.01;
		
//		elevator.set(motorSpeed);
	}
	
	/**
	 * Sets both left motors to a speed
	 * @param speed that both motors will be set to
	 */
	public void setLeftDrive(double speed) {
		leftDrive1.set(speed);
		leftDrive2.set(speed);
	}
	
	/**
	 * Sets both right motors to a speed
	 * @param speed that both motors will be set to
	 */
	public void setRightDrive(double speed) {
		rightDrive1.set(speed);
		rightDrive2.set(speed);
	}

	/**
	 * Raise a number to a power and preserve the sign
	 * @param power exponent
	 * @param num number to be exponentiated
	 * @return the num ^ power with the same sign as num
	 */
	public double powerPreserveSign(double num, double power) {
		double out = Math.pow(num, power);
		if ((num > 0) != (out > 0))
			out = out * -1;
		return out;
	}




	// =================================================================
	// =======================Autonomous Code===========================
	// =================================================================
	
	public void drivePastLine() {
		
	}
	
	public void driveForwardTimed(double time) {
		
	}




}
