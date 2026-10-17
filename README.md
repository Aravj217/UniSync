# UniSync: College Event, Volunteer & Team Coordination System

> **Events, People, Participation — In Sync.**

UniSync is a comprehensive Android application engineered for university campuses (NMIMS) to streamline event creation, team support requests, volunteer allocation, and administrative governance.

---

## 🔑 Demo Login Accounts

For testing, evaluation, and demonstrations, three pre-configured accounts are provided with one-tap auto-fill buttons directly on the Login screen:

| Role | Target Dashboard | Demo Email | Password |
|---|---|---|---|
| **🛡️ Admin** | `AdminDashboardActivity` | `admin@unisync.com` | `admin123` |
| **📋 Event Manager (Organizer)** | `OrganizerDashboardActivity` | `organizer@unisync.com` | `organizer123` |
| **🎓 Student (User)** | `StudentDashboardActivity` | `student@unisync.com` | `student123` |

> 💡 **Quick Fill:** On the Login screen, simply tap the **Admin**, **Manager**, or **Student** button at the bottom of the card to instantly auto-fill the credentials and press **Login**!

---

## 🏛️ System Architecture & User Roles

UniSync operates on a role-based access control (RBAC) model with three dedicated user profiles:

### 1. 🛡️ Admin
- **Central Governance:** Full administrative dashboard with real-time statistics (total events, pending approvals, published events, pending support requests, approved requests, total volunteers).
- **Event Approvals:** Review event submissions with full event dossiers. Option to Approve, Approve & Publish Immediately, or Reject with feedback reasons.
- **Support Request Approvals:** Review logistics/preparation support requests with individual student rosters. Approve, Return for Correction, or Reject.
- **User Management:** Manage all campus users across Admin, Organizer, and Student roles with active/disabled state toggles.
- **Campus Auditing:** Comprehensive directory of all campus events and volunteers with full search and status filters. Every decision generates an immutable `administrativeLogs` entry.

### 2. 📋 Organizer
- **Prominent Action Hub:** Two primary action triggers: **Create Event** and **Request Team Support**.
- **Event Lifecycle:** Draft → Submit for Approval → Approved → Publish to Students.
- **Support Request Builder:** Multi-category requests (Stage Setup, Sound, Tech, Decoration, Crowd Control, Logistics, etc.) with pre-selected rosters or open volunteer requests.
- **Volunteer Management:** Search, assign, edit, and organize student volunteers into teams and duties.

### 3. 🎓 Student
- **Event Discovery:** Browse published events with real-time text search and category filtering chips (Technical, Cultural, Sports, Workshop, etc.).
- **Event Engagement:** View complete event schedule, venue, organizing club, and register/volunteer directly with auto-populated profile information.
- **Volunteer Opportunities:** View approved open team support calls across campus and sign up for duties.
- **My Assignments:** Track personal volunteer duties and assigned event teams.

---

## 📂 Project Structure

```
d:/UniSync/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/example/unisync/
│   │   │   ├── UniSyncApplication.java
│   │   │   ├── activities/
│   │   │   │   ├── auth/
│   │   │   │   │   ├── SplashActivity.java
│   │   │   │   │   ├── LoginActivity.java
│   │   │   │   │   ├── RegisterActivity.java
│   │   │   │   │   └── ProfileActivity.java
│   │   │   │   ├── admin/
│   │   │   │   │   ├── AdminDashboardActivity.java
│   │   │   │   │   ├── EventApprovalListActivity.java
│   │   │   │   │   ├── EventReviewActivity.java
│   │   │   │   │   ├── RequestApprovalListActivity.java
│   │   │   │   │   ├── RequestReviewActivity.java
│   │   │   │   │   ├── ManageUsersActivity.java
│   │   │   │   │   ├── AllEventsActivity.java
│   │   │   │   │   └── AllVolunteersActivity.java
│   │   │   │   ├── organizer/
│   │   │   │   │   ├── OrganizerDashboardActivity.java
│   │   │   │   │   ├── CreateEventActivity.java
│   │   │   │   │   ├── MyEventsActivity.java
│   │   │   │   │   ├── EventDetailActivity.java
│   │   │   │   │   ├── CreateSupportRequestActivity.java
│   │   │   │   │   ├── MySupportRequestsActivity.java
│   │   │   │   │   ├── SupportRequestDetailActivity.java
│   │   │   │   │   ├── ManageVolunteersActivity.java
│   │   │   │   │   └── AddVolunteerActivity.java
│   │   │   │   └── student/
│   │   │   │       ├── StudentDashboardActivity.java
│   │   │   │       ├── EventBrowseActivity.java
│   │   │   │       ├── EventViewActivity.java
│   │   │   │       ├── PublicRequestsActivity.java
│   │   │   │       └── MyAssignmentsActivity.java
│   │   │   ├── adapters/
│   │   │   │   ├── EventCardAdapter.java
│   │   │   │   ├── TeamRequestAdapter.java
│   │   │   │   ├── VolunteerAdapter.java
│   │   │   │   ├── UserAdapter.java
│   │   │   │   └── RequestPersonAdapter.java
│   │   │   ├── models/
│   │   │   │   ├── User.java
│   │   │   │   ├── Event.java
│   │   │   │   ├── TeamRequest.java
│   │   │   │   ├── RequestRole.java
│   │   │   │   ├── RequestPerson.java
│   │   │   │   ├── Volunteer.java
│   │   │   │   └── AdminLog.java
│   │   │   └── utils/
│   │   │       ├── Constants.java
│   │   │       ├── FirestoreHelper.java
│   │   │       ├── SessionManager.java
│   │   │       └── UIHelper.java
│   │   └── res/
│   │       ├── layout/
│   │       ├── drawable/
│   │       └── values/ (colors, strings, styles, dimens)
```

---

## 🗄️ Firestore Database Schema

- **`users/{uid}`**: User profiles (fullName, email, role, course, year, department, rollNumber, active, createdAt).
- **`events/{eventId}`**: Events (eventName, category, shortDescription, description, eventDate, startTime, endTime, venue, organizingTeam, facultyInCharge, organizerId, status, rejectionReason, reviewedBy, reviewedAt, publishedAt).
- **`teamRequests/{requestId}`**: Support requests (title, category, reason, requiredDate, peopleRequired, workLocation, additionalInstructions, status, organizerId, relatedEventId).
  - Subcollection **`people/{personId}`**: Dynamic roster members (fullName, course, year, department, rollNumber, assignedDuty, team).
- **`volunteers/{volunteerId}`**: Event volunteer assignments (studentName, course, year, department, rollNumber, eventId, assignedDuty, team, createdBy).
- **`administrativeLogs/{logId}`**: Audit trail (actorUid, action, targetType, targetId, previousStatus, newStatus, reason, timestamp).

---

## 🚀 Setup & Execution

1. **Firebase Configuration:**
   Place your project's `google-services.json` inside the `app/` folder:
   ```
   UniSync/app/google-services.json
   ```
2. **Firestore Security Rules:**
   To ensure Firebase allows read/write access during testing and demonstrations:
   - Go to **Firebase Console** → **Firestore Database** → **Rules** tab.
   - Copy and paste the contents of `firestore.rules` from this project:
     ```
     rules_version = '2';
     service cloud.firestore {
       match /databases/{database}/documents {
         match /{document=**} {
           allow read, write: if true;
         }
       }
     }
     ```
   - Click **Publish**.

3. **Resilient Data Architecture (`DataManager`):**
   - UniSync includes a dual-layer synchronization system (`DataManager`).
   - If Firestore permissions are locked or network is unreachable, UniSync automatically and silently persists and retrieves events, support requests, volunteers, and users locally.
   - You will **never** see raw permission denied toasts or crash states; clean empty states and fallback data are displayed seamlessly.

4. **Open in Android Studio:**
   - Open the `UniSync` folder in Android Studio.
   - Sync Gradle project with build files.
5. **Build & Run:**
   - Select an Android emulator (API 24+) or a physical device with USB debugging enabled.
   - Run `app` (`Shift + F10`).
