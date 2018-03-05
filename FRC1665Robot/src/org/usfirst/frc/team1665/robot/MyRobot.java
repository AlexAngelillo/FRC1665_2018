package org.usfirst.frc.team1665.robot;

/**
 * Code for FRC 1665's 2018 robot
 * @author Alex Angelillo
 */

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.VictorSP;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.DigitalInput;



enum drivePastLineEnum {
	INIT, DRIVE, STOP
}

enum boxAutoEnum {
	INIT, DRIVE, TURN, DRIVE2, TURN2, LIFT, DRIVE3, DROP;
}

public class MyRobot {
	// Motor pin declarations
	final int leftDrivePin1 = 1;
	final int leftDrivePin2 = 2;
	final int rightDrivePin1 = 3;
	final int rightDrivePin2 = 4;
	final int intake1Pin = 7;
	final int intake2Pin = 8;
	final int climberPin = 5;
	final int elevatorPin = 6;
	
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
	
	Solenoid ds1;
	Solenoid gripper2;
	Solenoid bar2;
	
	// Sensors
	Encoder elevatorEncoder;
	DigitalInput ls;
	
	// Helper variables
	boolean gripperActionPrevious = false;
	boolean gripperAction2Previous = false;
	boolean previousAdjustmentMade = false;
	boolean intakeOn = false;
	boolean intakePrevious = false;
	double eleSetpoint = 10.0;
	boolean home = false;
	
	// Auto
	drivePastLineEnum autoState = drivePastLineEnum.INIT;
	Timer timer;
	double time = 0.0;
	
	
	
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
		
		intake1.setInverted(true);
		intake2.setInverted(true);
		
		elevator.setInverted(true);
		
		
		// Invert the left side so positive is forward
		rightDrive1.setInverted(true);
		rightDrive2.setInverted(true);
		
		// Compressor and pneumatics
		compressor = new Compressor(0);
		climberLock = new Solenoid(1);
		
		gripper = new Solenoid(3);
		ds1 = new Solenoid(2);
		gripper2 = new Solenoid(4);
		bar = new Solenoid(5);
		bar2 = new Solenoid(6);
		
		
		
		// Pneumatics initial positions and turn on compressor
		compressor.setClosedLoopControl(true);
		climberLock.set(false);
		ds1.set(true);
		gripper.set(false);
		gripper2.set(true);
		
		bar.set(false);
		bar2.set(true);
		
		
		// Encoder setup
		elevatorEncoder = new Encoder(8,9,false,Encoder.EncodingType.k1X);
		elevatorEncoder.reset();
		// Limit Switch
		ls = new DigitalInput(2);
		
		// Setup timer
		timer = new Timer();
		timer.reset();
		timer.start();
	}
	
	
	/**
	 * Teleop control
	 */
	public void teleop() {
		baseDriveControl();
		intakeControl();
		climberControl();
		elevatorControl();
	}
	
	
	/**
	 * Drive during teleop
	 */
	public void baseDriveControl() {
		// Get joystick input
//		double leftPower = driverJoystick.getRawAxis(5);
//		double rightPower = driverJoystick.getRawAxis(1);
		
		double forward = driverJoystick.getRawAxis(5);
		double turn = driverJoystick.getRawAxis(0);
		
		if (Math.abs(forward) < 0.05)
			forward = 0;
		if (Math.abs(turn) < 0.05)
			turn = 0;
		
		forward = powerPreserveSign(forward, 2);
		turn = powerPreserveSign(turn,2);
		
		double leftPower = forward + turn;
		double rightPower = forward - turn;
		
		
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
		
		double intakeSpeed = 1;
		double outtakeSpeed = -1;
		
		/* Toggle on, touch off
		if (intake && !intakePrevious) {
			if (!intakeOn) {
				intake1.set(intakeSpeed);
				intake2.set(intakeSpeed);
			} else {
				intake1.set(0);
				intake2.set(0);
			}
			intakeOn = !intakeOn;
		} else if (outtake) {
			intakeOn = false;
			intake1.set(outtakeSpeed);
			intake2.set(outtakeSpeed);
		} else if (!outtake && !intake && !intakeOn) {
			intake1.set(0);
			intake2.set(0);
		}
		*/
		
		// Intake rollers
		if (intake) {
			intake1.set(intakeSpeed);
			intake2.set(intakeSpeed);
		} else if (outtake) {
			intake1.set(outtakeSpeed);
			intake2.set(outtakeSpeed);
		} else {
			intake1.set(0);
			intake2.set(0);
		}
		

		// Gripper pneumatics
		if (gripperAction && !gripperActionPrevious) {
			System.out.println("Gripper action " + gripper.get());
			gripper.set(!gripper.get());
			gripper2.set(!gripper2.get());
//			System.out.println(gripper.get() + "\t\t" + gripper2.get());
		}
		
		gripperActionPrevious = gripperAction;
		intakePrevious = intake;
	}

	/**
	 * Controls the climber and latches during teleop
	 */
	public void climberControl() {
		// Get joystick input
		boolean latch = driverJoystick.getRawButton(3);
		boolean climbUp = operatorJoystick.getRawButton(5);
		boolean climbDown = operatorJoystick.getRawButton(6);
		boolean releaseArmBar = driverJoystick.getRawButton(1);
		
		// Climber
		if (climbUp) {
			climber.set(1);
		} else if (climbDown) {
			climber.set(-1);
		} else {
			climber.set(0);
		}
		
		// Climber latch
		if (latch) {
			System.out.println("Latch enabling");
			climberLock.set(true);
			ds1.set(false);
		}
		
		// Arm bar control for hoisting other robots
		if (releaseArmBar) {
			System.out.println("Arm bar out");
			bar.set(true);
			bar2.set(false);
		}
	}
	
	
	/**
	 * Elevator proportion control
	 */
	public void elevatorControl() {
		
		
		/*
		// Manual no encoder control
		if (operatorJoystick.getRawAxis(2) > 0.5) {
			elevator.set(1);
		} else if (operatorJoystick.getRawAxis(3) > 0.5) {
			elevator.set(-1);
		} else {
			elevator.set(0);
		}
		*/
		
		
		// Proportion control
		double pv = elevatorEncoder.getRaw();
		
		// Preset heights
		if (operatorJoystick.getRawButton(1))
			home = true;
		else if (operatorJoystick.getRawButton(2))
			eleSetpoint = 430;
		else if (operatorJoystick.getRawButton(4))
			eleSetpoint = 1300;
		
		// Adjustments
		if (operatorJoystick.getRawAxis(2) > 0.5 && !previousAdjustmentMade) {
			eleSetpoint += 50;
			previousAdjustmentMade = true;
		} else if (operatorJoystick.getRawAxis(3) > 0.5 && !previousAdjustmentMade) {
			eleSetpoint -= 50;
			previousAdjustmentMade = true;
		} else if (operatorJoystick.getRawAxis(3) < 0.5 && operatorJoystick.getRawAxis(2) < 0.5)
			previousAdjustmentMade = false;
		
		// Keep the lower bound
		if (eleSetpoint < 10)
			eleSetpoint = 10;


		// Proportion control calculations
		double error = eleSetpoint - pv;
		double motorSpeed = error * 0.05;
		
		System.out.println(pv + "\t\t" + eleSetpoint + "\t\t" + motorSpeed);
		
		// Home the elevator every time it goes all the way down, the all the way
		// down control will go until the limit switch is pressed and reset the
		// encoder to 0
		if (home) {
			elevator.set(-1);
			if (ls.get()) {
				home = false;
				elevatorEncoder.reset();
				eleSetpoint = 20;
			}
		}
		// If not homing just use the proportional value
		else
			elevator.set(motorSpeed);
		
		
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
	
	/*
	 * Drives past the auto line, more accurately drives for a certain
	 * duration of time.
	 */
	public void drivePastLine(double duration) {
		System.out.println(autoState + "\t\t" + timer.get());
		switch(autoState) {
		case INIT:
			time = timer.get();
			autoState = drivePastLineEnum.DRIVE;
			break;
		case DRIVE:
			if (timer.get() - time < 2.2) {
				setLeftDrive(-0.6);
				setRightDrive(-0.5);
			} else {
				setLeftDrive(0);
				setRightDrive(0);
				time = timer.get();
				autoState = drivePastLineEnum.STOP;
			}
			break;
		case STOP:
			break;
		default:
			break;
			
		}
	}

	/**
	 * Attempts to determine which side of the scale matches the alliance
	 * and place a cube accordingly.
	 */
	public void dropBoxInScale() {
		
	}
}