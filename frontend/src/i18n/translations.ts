export const translations = {
  en: {
    "app.title": "Workflow Demo Console",
    "app.subtitle": "Camunda workflow demo: Start → Approval → Manual Completion → End",
    "app.currentIdentity": "Current Identity",

    "tabs.start": "Start",
    "tabs.approval": "Approvals",
    "tabs.manual": "Manual Tasks",
    "tabs.status": "Status",

    "identity.initiator": "Initiator",
    "identity.approver": "Approver",
    "identity.executor": "Executor",
    "identity.role.hint": "{{userId}}",

    "start.heading": "Start Process",
    "start.description": "{{name}} ({{userId}}) will be used as the process initiator.",
    "start.approver": "Approver",
    "start.executor": "Manual executor",
    "start.payload": "Business payload (JSON)",
    "start.launch": "Start process",
    "start.launch.loading": "Starting...",
    "start.reset": "Reset",
    "start.success": "Process {{id}} has been created (state: {{state}})",
    "start.error": "Failed to start process: {{message}}",

    "tasks.heading.approval": "Approval Tasks",
    "tasks.heading.manual": "Manual Tasks",
    "tasks.role.info": "Acting as {{name}} ({{userId}})",
    "tasks.refresh": "Refresh queue",
    "tasks.refresh.loading": "Loading...",
    "tasks.empty": "No tasks currently assigned",
    "tasks.load.error": "Failed to load tasks: {{message}}",
    "tasks.permission.denied": "The current identity does not have permission for this view. Switch persona to continue.",
    "tasks.operation.success": "Task processed successfully",
    "tasks.operation.error": "Task operation failed: {{message}}",
    "tasks.approve": "Approve",
    "tasks.reject": "Reject",
    "tasks.complete": "Complete task",
    "tasks.approve.prompt": "Approval comment (optional)",
    "tasks.reject.prompt": "Rejection reason",
    "tasks.complete.prompt": "Completion comment (optional)",
    "tasks.approve.log": "Task {{taskId}} approved by {{userId}}",
    "tasks.reject.log": "Task {{taskId}} rejected by {{userId}}: {{reason}}",
    "tasks.complete.log": "Task {{taskId}} completed by {{userId}}",

    "status.heading": "Process Status",
    "status.description": "Look up the current node, history and variables for a process instance.",
    "status.field.processId": "Process instance ID",
    "status.fetch": "Fetch status",
    "status.fetch.loading": "Loading...",
    "status.fetch.success": "Status refreshed",
    "status.fetch.error": "Unable to retrieve status: {{message}}",
    "status.timeline.empty": "No historical records",
    "status.variables": "Process variables",
    "status.currentTask": "Current task",
    "status.timeline": "Timeline",
    "status.handler": "Assignee",
    "status.result": "Result",
    "status.start": "Started",
    "status.end": "Finished",

    "log.title": "Activity Log",
    "log.caption": "Local session entries",
    "log.empty": "No actions recorded yet",

    "statusTag.pending": "Pending",
    "statusTag.manual": "Manual",
    "statusTag.completed": "Completed",
    "statusTag.rejected": "Rejected",
    "statusTag.unknown": "Unknown",

    "start.log.switch": "Switching to status tab to follow process {{id}}",
    "status.log.refresh": "Status refreshed for {{id}}: {{state}}",
    "status.log.refresh.error": "Failed to refresh status: {{message}}",

    "tasks.prompt.approvalDefault": "Approved",
    "tasks.prompt.rejectDefault": "Incomplete information",
    "tasks.prompt.completeDefault": "Manual step completed"
  }
} as const;

export type SupportedLocale = keyof typeof translations;
export type TranslationKey = keyof (typeof translations)["en"];
