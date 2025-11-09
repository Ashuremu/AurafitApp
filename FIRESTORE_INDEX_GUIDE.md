# Firestore Index Configuration Guide

## Issue
The reservation queries are failing because Firestore requires composite indexes for compound queries (queries that filter by one field and order by another).

## Error Message
```
FAILED_PRECONDITION: The query requires an index. You can create it here: https://console.firebase.google.com/v1/r/project/aurafit-7ab57/firestore/indexes?create_composite=...
```

## Solution 1: Use the Fixed Code (Recommended)
The code has been updated to work without requiring composite indexes by:
- Removing `orderBy` from Firestore queries and sorting in memory instead
- Reducing the number of `whereEqualTo` conditions in compound queries
- Filtering additional conditions in memory

## Solution 2: Create Required Indexes (For Better Performance)

### Index 1: User Reservations with Ordering
**Collection:** `reservations`
**Fields:**
- `userId` (Ascending)
- `reservedAt` (Descending)

**Purpose:** For querying user reservations ordered by date

### Index 2: Duplicate Reservation Check
**Collection:** `reservations`
**Fields:**
- `userId` (Ascending)
- `itemId` (Ascending)
- `selectedSize` (Ascending)
- `status` (Ascending)

**Purpose:** For checking duplicate reservations

## How to Create Indexes

### Method 1: Using Firebase Console
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project: `aurafit-7ab57`
3. Navigate to Firestore Database
4. Go to "Indexes" tab
5. Click "Create Index"
6. Configure the fields as specified above

### Method 2: Using the Error Link
1. Click the link provided in the error message
2. It will automatically open the Firebase Console with the correct index configuration
3. Click "Create Index"

### Method 3: Using Firebase CLI
```bash
# Install Firebase CLI if not already installed
npm install -g firebase-tools

# Login to Firebase
firebase login

# Initialize Firestore indexes
firebase init firestore

# Deploy indexes
firebase deploy --only firestore:indexes
```

## Performance Considerations

### Current Implementation (No Index Required)
- ✅ Works immediately without setup
- ✅ No additional Firestore costs for indexes
- ❌ Slightly slower for large datasets (in-memory sorting)
- ❌ Downloads all user reservations before sorting

### With Composite Indexes
- ✅ Faster queries for large datasets
- ✅ Server-side sorting and filtering
- ✅ Better performance for pagination
- ❌ Requires index creation and maintenance
- ❌ Additional Firestore costs

## Recommended Approach

1. **For Development/Testing:** Use the current fixed implementation (no indexes required)
2. **For Production:** Create the composite indexes for better performance

## Code Usage

### Current Implementation (No Index Required)
```java
// This works without any index setup
reservationService.getUserReservations(successListener, failureListener);
```

### With Indexes (Better Performance)
```java
// Use this if you've created the required indexes
reservationService.getUserReservationsWithOrdering(successListener, failureListener);
```

## Monitoring Index Usage

After creating indexes, monitor their usage in the Firebase Console:
1. Go to Firestore Database
2. Click on "Usage" tab
3. Check "Indexes" section for performance metrics

## Troubleshooting

### Index Creation Failed
- Ensure you have proper permissions in the Firebase project
- Check that the collection name and field names match exactly
- Verify the field types are correct

### Query Still Failing
- Wait a few minutes for the index to be built
- Check the Firebase Console for index status
- Verify the query matches the index configuration exactly

### Performance Issues
- Monitor index usage in Firebase Console
- Consider adding more specific indexes for frequently used queries
- Use pagination for large result sets

