# TheLittleThings

TheLittleThings is a goal tracking and social motivation application we've developed for our Advanced Software Development course. The idea behind the app is to help users set personal goals, track their progress through journaling, and stay motivated by connecting with friends through leaderboards and challenges.

## Project Structure

We've organized this project as a monorepo with two main parts - a React frontend and a Spring Boot backend. This setup makes it easier to manage both sides of the application while keeping everything in one place.

```
TheLittleThings/
├── frontend/           // React + TypeScript frontend application
├── backend/           // Spring Boot backend API
├── azure-pipelines.yml // CI/CD configuration
├── package.json       // Root workspace configuration
└── README.md         // This file
```

### Frontend Setup

The frontend is built with React 19 and TypeScript, using Vite as our build tool. We chose Tailwind CSS for styling because it makes the UI development much faster and more consistent. React Router DOM handles all the navigation between different pages.

Our main features include:
- User authentication system (sign in and registration)
- Goal creation and management with categories
- Personal journal entries for reflection
- Friends system where users can connect with each other
- Leaderboards to create some friendly competition
- User profiles and customizable settings

The frontend code is organized like this:
```
frontend/src/
├── components/        // Reusable UI components
│   ├── buttons/      // Button components
│   ├── categories/   // Category-related components
│   ├── goals/        // Goal management components
│   ├── journals/     // Journal entry components
│   └── ui/          // General UI components
├── pages/            // Route components
├── api/              // API client functions
├── layouts/          // Layout components
├── routes/           // Route configurations
├── lib/              // Utility functions
└── styles/           // CSS and styling files
```

### Backend Architecture

For the backend, we're using Spring Boot 3.3.3 with Java 17. PostgreSQL handles our data storage, and we use JPA/Hibernate for database operations. Maven manages our dependencies and build process.

The backend provides:
- RESTful API endpoints for all frontend operations
- User authentication and authorization
- Data persistence with proper validation
- Comprehensive test coverage to ensure reliability

Our backend structure follows standard Spring Boot conventions:
```
backend/src/main/java/com/project/thelittlethings/
├── controller/        // REST API controllers
├── services/         // Business logic layer
├── repositories/     // JPA repositories
├── entities/         // JPA entity classes
├── dto/             // Data Transfer Objects
├── security/        // Authentication & authorization
├── config/          // Configuration classes
├── View/            // Database views
└── MaterialisedView/ // Materialized views
```

The main entities in our system are User (for account management), Goal (personal objectives), Category (goal organization), Win (achievement tracking), Journal (personal entries), Friendship (social connections), and various challenge-related entities.

## Getting Started

To run this project locally, you'll need Node.js 18+, Java 17+, Maven 3.6+, and PostgreSQL 12+.

For the frontend:
```bash
cd frontend
npm install
npm run dev
```

For the backend:
```bash
cd backend
./mvnw spring-boot:run
```


## Team Responsibilities

We've divided the work among our team members based on different feature areas:

//Ilker Paken (25527603)//
Ilker is handling the wins tracking system and leaderboard functionality. This includes Epic 9 (wins management) where completed goals get logged as wins, and Epic 31 (leaderboard) that ranks users based on their trophies and achievements. He's working on the Leaderboard.tsx and Wins.tsx components on the frontend, plus the WinService, WinController, and LeaderboardService on the backend.

//Maxim Tabachuk (24615078)//
Maxim is responsible for the streak system and social features. His work covers Epic 60 (automatic streaks, challenges and trophies) which tracks user activity streaks and resets them when inactive, and Epic 13 (friends management) for social connections and friend challenges. He's developing the FriendsPage.tsx component and various streak display components, along with the FriendshipService and ChallengeService backend logic.

//Ali Idrees (24545790)//
Ali is working on user management and the journaling system. He's handling Epic 12 (journaling and self reflection) where users can write, read, and manage their personal thoughts with sorting and reminder features, and Epic 4 (user management) covering sign up, login, profile management, and account operations. His components include Journal.tsx, SignIn.tsx, Register.tsx, and UserProfile.tsx, with corresponding UserService, UserController, JournalService, and JournalController backend services.

//Naseem Win (24964684)//
Naseem is taking care of goals and category management. His responsibilities include Epic 8 (goals management) for creating, filtering, updating and prioritizing goals, and Epic 34 (category management) for organizing goals into meaningful groups. He's developing Goals.tsx and Categories.tsx components along with the GoalService, GoalController, CategoryService, and CategoryController backend services.

//Nasser Al Mughairi (24605154)//
Nasser is handling user settings and notifications. He's working on Epic 11 (settings) for profile management, account security, and notification preferences, and Epic 10 (notifications & reminders) for daily reminders, milestone alerts, and social notifications. His work includes the Settings.tsx component and various notification-related components, plus the backend settings management and notification services.

