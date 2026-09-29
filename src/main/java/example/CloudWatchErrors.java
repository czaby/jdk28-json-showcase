package example;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.OrderBy;

/** Latest CloudWatch log stream in a group, then ERROR / WARNING / Exception. */
public final class CloudWatchErrors {
    public static void main(String[] args) {
        var group = args.length > 0 ? args[0] : System.getenv().getOrDefault("LOG_GROUP", "/aws/lambda/demo");
        try (var logs = CloudWatchLogsClient.create()) {
            var streams = logs.describeLogStreams(r -> r
                    .logGroupName(group)
                    .orderBy(OrderBy.LAST_EVENT_TIME)
                    .descending(true)
                    .limit(1))
                .logStreams();
            if (streams.isEmpty()) {
                System.out.println("no streams in " + group);
                return;
            }
            var stream = streams.getFirst().logStreamName();
            System.out.println("stream: " + stream);
            logs.filterLogEvents(r -> r
                    .logGroupName(group)
                    .logStreamNames(stream)
                    .filterPattern("?ERROR ?WARNING ?Exception"))
                .events()
                .forEach(e -> System.out.println(e.timestamp() + " " + e.message()));
        }
    }
}
