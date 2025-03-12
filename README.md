# TaskTracker 2.0

TaskTracker is a comprehensive Android task management application built with Kotlin. This project was originally developed as a course assignment and expanded into a full-featured application.

## Features

- **Task Management**: Create, edit, and delete tasks with details
- **Priority Levels**: Assign LOW, MEDIUM, or HIGH priority to tasks
- **Due Dates**: Set and track task due dates
- **Reminders**: Get notifications when tasks are due
- **Task Completion**: Mark tasks as completed with visual feedback
- **Search Functionality**: Search through your tasks
- **Task Sorting**: View tasks sorted by due date
- **Undo Deletion**: Restore accidentally deleted tasks

## Architecture

The application is built using modern Android development practices:

- **MVVM Architecture**: Uses ViewModel and LiveData for reactive UI updates
- **Room Database**: Local persistence using SQLite with Room ORM
- **Fragments**: UI components built with fragments for reusability
- **RecyclerView**: Efficient display of scrollable lists
- **Material Design**: Modern UI following Material Design guidelines

## Screenshots

**Task List UI:** Displays all tasks with due date, priority, and completion status. ![Screenshot](https://github.com/CJ020328/TaskTrackerApp/blob/main/TTAScreenShot/TTA_img1.png)

**Sorting & Filtering:** Users can sort tasks by priority or due date. ![Screenshot](https://github.com/CJ020328/TaskTrackerApp/blob/main/TTAScreenShot/TTA_img2.png)

**Task Creation:** Allows users to define a new task with reminders. ![Screenshot](https://github.com/CJ020328/TaskTrackerApp/blob/main/TTAScreenShot/TTA_img3.png)

**Task Completion & Deletion:** Tasks can be marked as done or removed. ![Screenshot](https://github.com/CJ020328/TaskTrackerApp/blob/main/TTAScreenShot/TTA_img4.png)

**Reminder Selection:** Custom time picker for task reminders. ![Screenshot](https://github.com/CJ020328/TaskTrackerApp/blob/main/TTAScreenShot/TTA_img5.png)

## Requirements

- Android 5.0 (API Level 21) or higher
- Android Studio 4.0 or higher

## Getting Started

### Prerequisites

- Android Studio
- Android SDK

### Installation

1. Clone the repository
```
git clone https://github.com/YourUsername/TaskTracker.git
```

2. Open the project in Android Studio

3. Build and run the application on your device or emulator

## Usage

- **Add a Task**: Tap the "+" floating action button to create a new task
- **Edit a Task**: Tap on any task in the list to edit its details
- **Complete a Task**: Check the checkbox next to a task
- **Delete a Task**: Tap the delete button when viewing a task
- **Set Reminders**: When creating or editing a task, set a reminder date and time
- **Search Tasks**: Use the search icon in the app bar to find specific tasks

## Technical Details

- **Data Storage**: Uses Room database to store task information
- **Notifications**: Implements AlarmManager and BroadcastReceiver for reminders
- **UI Components**: Utilizes RecyclerView, CardView, and other material components
- **Animations**: Includes task list animations and transitions

## Development History

This project started as a course assignment to demonstrate understanding of Android development concepts. It has since been expanded with additional features and improved architecture to create a fully functional task management application.

## License

This project is for educational purposes.

## Acknowledgements

- Material Design Components
- AndroidX Libraries
- Room Persistence Library 
