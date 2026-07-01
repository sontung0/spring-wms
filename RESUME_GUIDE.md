# How to Resume Your Spring Integration Lessons

Welcome back! To resume where we left off, all the state has been preserved in this workspace.

## 1. Review Your Progress
You can remind yourself of what we've covered by looking at the HTML lessons in the `lessons/` directory:
- `0001-the-core-trifecta.html` (Messages, Channels, Endpoints)
- `0002-hello-world-dsl.html` (IntegrationFlow builder)
- `0003-gateways.html` (@MessagingGateway)
- `0004-routing.html` (.route payload mapping)
- `0005-service-activators.html` (.handle with POJOs)
- `0006-message-headers.html` (.enrichHeaders)
- `0007-message-filters.html` (.filter with discard channels)

## 2. Review the Code
All the code we wrote is located in:
`src/main/java/com/example/integration/`
You can run it at any time using:
`mvn spring-boot:run`

## 3. How to Prompt the Agent
When you start a **new chat session**, just give the agent this exact prompt to get right back into the flow:

> "I want to resume my Spring Integration learning journey in the `spring-wms` workspace. Please check the `learning-records` and `NOTES.md` to see where we left off, and generate Lesson 8 (which should be about Error Handling)."