# Scheduled Timer Flow - Sequential Call List

## Sequential Function Calls

| # | Class | Method | Parameters | Return |
|---|-------|--------|-----------|--------|
| 1 | EmailProcessingSchedulerService | processEmails() | - | void |
| 2 | UserRepository | findAll() | - | List<User> |
| 3 | EmailProcessingSchedulerService | hasValidAccessToken(user) | User | boolean |
| 4 | EmailProcessingSchedulerService | processEmailsForUser(user) | User | void |
| 5 | PlatformRepository | findByUserAndIsDeletedFalse(user) | User | List<Platform> |
| 6 | CategoryRepository | findByUserAndIsDeletedFalse(user) | User | List<Category> |
| 7 | EmailProcessingSchedulerService | processUserEmails(user, platforms, categories) | User, List<Platform>, List<String> | void |
| 8 | EmailProcessingSchedulerService | processGmailMessagesDirectly(user, platforms, categories) | User, List<Platform>, List<String> | void |
| 9 | GmailService | fetchEmailsForProcessing(user) | User | List<EmailData> |
| 10 | GmailService | getGmailService(user) | User | Gmail |
| 11 | Gmail API | list("me") | maxResults, query | ListMessagesResponse |
| 12 | GmailService | extractEmailData(service, message) | Gmail, Message | EmailData |
| 13 | EmailProcessingSchedulerService | isPlatformAllowed(senderEmail, platforms) | String, List<Platform> | boolean |
| 14 | EmailProcessingSchedulerService | processEmailDirectly(emailData, user, platforms, categories) | EmailData, User, List<Platform>, List<String> | void |
| 15 | GeminiEmailAnalysisService | analyzeEmail(body, subject, sender, categories) | String, String, String, List<String> | EmailAnalysisResult |
| 16 | EmailAnalysisResult | isOrderRelatedEmail() | - | boolean |
| 17 | EmailProcessingSchedulerService | findMatchingPlatform(senderEmail, platforms) | String, List<Platform> | Platform |
| 18 | EmailProcessingSchedulerService | handleOrderCreationOrUpdate(user, analysis, platform) | User, EmailAnalysisResult, Platform | void |
| 19 | OrderRepository | findByOrderId(orderId) | String | Optional<Order> |
| 20a | EmailProcessingSchedulerService | updateOrder(order, analysis, user, platform) | Order, EmailAnalysisResult, User, Platform | void |
| 20b | EmailProcessingSchedulerService | createNewOrder(user, analysis, platform) | User, EmailAnalysisResult, Platform | Order |
| 21a/b | OrderRepository | save(order) | Order | Order |
| 22a/b | PlatformRepository | findById(platformId) | Long | Optional<Platform> |
| 23 | CategoryRepository | findByUserAndIsDeletedFalse(user) | User | List<Category> |
| 24 | VendorRepository | findByVendorName(vendorName) | String | Optional<Vendor> |
| 25 | EmailProcessingSchedulerService | parseDateTime(dateTimeStr) | String | LocalDateTime |
| 26a/b | NotificationTemplates | getTemplate(status, orderId, productName) | String, String, String | NotificationTemplate |
| 27a/b | NotificationService | sendNotification(userId, title, body) | String, String, String | void |
| 28 | UserRepository | findByUserId(userId) | String | Optional<User> |
| 29 | FirebaseMessaging | send(message) | Message | String |
| 30 | GmailService | archiveEmail(user, messageId) | User, String | void |
| 31 | Gmail API | modify("me", messageId, request) | String, String, ModifyMessageRequest | Message |
| 32 | EmailProcessingSchedulerService | updateLastProcessedEmailTimestamp(user) | User | void |
| 33 | UserConfigRepository | save(userConfig) | UserConfig | UserConfig |

