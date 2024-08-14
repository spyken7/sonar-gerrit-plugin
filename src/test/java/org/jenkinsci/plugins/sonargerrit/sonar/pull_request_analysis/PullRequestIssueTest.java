package org.jenkinsci.plugins.sonargerrit.sonar.pull_request_analysis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import me.redaalaoui.org.sonarqube.ws.Issues;
import me.redaalaoui.org.sonarqube.ws.ProjectPullRequests;
import me.redaalaoui.org.sonarqube.ws.client.WsConnector;
import me.redaalaoui.org.sonarqube.ws.client.WsRequest;
import me.redaalaoui.org.sonarqube.ws.client.WsResponse;
import me.redaalaoui.org.sonarqube.ws.client.issues.IssuesService;
import me.redaalaoui.org.sonarqube.ws.client.issues.SearchRequest;
import org.jenkinsci.plugins.sonargerrit.sonar.Components;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** @author Réda Housni Alaoui */
class PullRequestIssueTest {

  private PullRequestIssue pullRequestIssue;

  @BeforeEach
  void beforeEach() {
    ProjectPullRequests.PullRequest pullRequest =
        ProjectPullRequests.PullRequest.newBuilder().setKey("pr-key").build();
    Components components = mock(Components.class);
    when(components.buildPrefixedPathForComponentWithKey(any(), any()))
        .thenReturn(Optional.absent());
    Issues.Issue issue =
        Issues.Issue.newBuilder().setProject("project-key").setKey("issue-key").build();

    pullRequestIssue =
        new PullRequestIssue(pullRequest, components, issue, "https://sonarqube.example.org");
  }

  @Test
  @DisplayName("Check detailUrl")
  void test1() {
    assertThat(pullRequestIssue.detailUrl())
        .contains(
            "https://sonarqube.example.org/project/issues?id=project-key&pullRequest=pr-key&open=issue-key");
  }

  @Test
  @DisplayName("Check inspectionId")
  void test2() {
    assertThat(pullRequestIssue.inspectionId()).isEqualTo("pr-key");
  }

  @Test
  void testSearchWithResolvedFalseCondition() {
    WsConnector wsConnector = mock(WsConnector.class);
    IssuesService issuesService = new IssuesService(wsConnector);

    WsResponse wsResponse = WsResponse.newBuilder().setResolved("false").build();
    when(wsConnector.call(any(WsRequest.class))).thenReturn(wsResponse);

    SearchRequest request = new SearchRequest().setResolved("false");
    Issues.SearchWsResponse response = issuesService.search(request);

    assertNotNull(response);

    SearchWsResponse expectedResponse = Issues.SearchWsResponse.newBuilder().setResolved("false").build();
    assertEquals(expectedResponse, response);
  }
}
