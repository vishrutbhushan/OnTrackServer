# OnTrack Server - Gemini AI Email Analysis Setup

## Overview
This implementation adds AI-powered email analysis using Google's Gemini Pro to extract order information from Gmail emails. The system automatically processes emails every 10 minutes to identify order-related emails and extract order IDs.

## Features
- ✅ **Gemini AI Integration**: Uses Gemini Pro for email analysis
- ✅ **Order ID Extraction**: Extracts order IDs and stores them in snippet field
- ✅ **Duplicate Prevention**: Tracks processed emails by Gmail Message ID
- ✅ **Scheduled Processing**: Runs every 10 minutes automatically
- ✅ **Free for Development**: Uses Gemini Pro free tier (60 requests/minute)

## Setup Instructions

### 1. Get Gemini API Key
1. Go to [Google AI Studio](https://makersuite.google.com/app/apikey)
2. Click "Create API Key"
3. Copy the generated key

### 2. Set Environment Variable
```bash
# Windows Command Prompt
set GEMINI_API_KEY=your_api_key_here

# Windows PowerShell
$env:GEMINI_API_KEY="your_api_key_here"

# Linux/Mac
export GEMINI_API_KEY=your_api_key_here
```

### 3. Database Schema Update
The application will automatically add these new columns to the `item` table:
- `gmail_message_id` (for tracking processed emails)
- `order_id` (for storing extracted order IDs)

### 4. Configuration
Email processing is configured in `application.properties`:
```properties
# Gemini AI Configuration
gemini.api.key=${GEMINI_API_KEY}

# Email Processing Configuration
email.processing.schedule.enabled=true
email.processing.schedule.interval=10  # minutes
```

## How It Works

### 1. Scheduled Processing
- Runs every 10 minutes (configurable)
- Checks all users with valid Gmail tokens
- Fetches only new emails (not already processed)
- Processes maximum 10 emails per user per run

### 2. AI Analysis
- Sends email content to Gemini Pro
- Extracts order-related information
- Identifies order IDs from various patterns:
  - `Order #123456`
  - `Order ID: ABC123`
  - `Confirmation #XYZ789`

### 3. Data Storage
- Only order-related emails are saved
- Order ID is stored in both `order_id` field and snippet
- Gmail Message ID prevents duplicate processing

## API Endpoints

### Test Gemini Analysis
```http
POST /api/items/test-analysis
Content-Type: application/json

{
  "subject": "Your Amazon order has shipped",
  "content": "Your order #123-4567890-1234567 has shipped. Item: iPhone 15 Pro.",
  "sender": "ship-confirm@amazon.com"
}
```

Response:
```json
{
  "orderRelatedEmail": true,
  "orderId": "123-4567890-1234567"
}
```

### Fetch and Process Emails
```http
POST /api/items/fetch/{userId}
```

## Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Gmail API     │────│  GmailService    │────│ Item Repository │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                                │
                                ▼
                       ┌──────────────────┐
                       │ Gemini Analysis  │
                       │    Service       │
                       └──────────────────┘
                                │
                                ▼
                       ┌──────────────────┐
                       │    Scheduler     │
                       │  (Every 10 min)  │
                       └──────────────────┘
```

## Monitoring

Check logs for processing status:
```
2025-08-27 10:00:00 - Starting scheduled email processing...
2025-08-27 10:00:01 - Processing emails for user: user123
2025-08-27 10:00:02 - Found 3 new emails for user: user123
2025-08-27 10:00:03 - Saved order-related email for user: user123 with order ID: AMZ-123456
```

## Troubleshooting

### Common Issues

1. **No API Key Error**
   - Ensure `GEMINI_API_KEY` environment variable is set
   - Restart the application after setting the variable

2. **Rate Limiting**
   - Gemini Pro free tier: 60 requests/minute
   - Reduce processing frequency if needed

3. **No Emails Processed**
   - Check if users have valid Gmail tokens
   - Verify scheduling is enabled in properties

### Debug Endpoints

- View user items: `GET /api/items/user/{userId}`
- Test analysis: `POST /api/items/test-analysis`

## Cost Considerations

- **Gemini Pro Free Tier**: 60 requests/minute
- **Estimated Usage**: ~1-5 requests per user per 10-minute cycle
- **Cost**: FREE for development/small projects

## Future Enhancements

- [ ] Extract additional fields (product name, seller, tracking number)
- [ ] Support for more email platforms
- [ ] Custom order ID patterns
- [ ] Email classification confidence scores
