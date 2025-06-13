package br.com.project.music.servlet;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.ListQueuesRequest;
import software.amazon.awssdk.services.sqs.model.ListQueuesResponse;
import software.amazon.awssdk.services.sqs.model.SqsException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Main {

    public static void main(String[] args) {
        Properties props = new Properties();
        try (InputStream input = Main.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                System.err.println("Erro: application.properties não encontrado no classpath.");
                System.exit(1);
            }
            props.load(input);
        } catch (IOException ex) {
            System.err.println("Erro ao carregar application.properties: " + ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }

        String awsRegionString = props.getProperty("aws.region");
        String awsAccessKey = props.getProperty("aws.s3.access-key");
        String awsSecretKey = props.getProperty("aws.s3.secret-key");

        if (awsRegionString == null || awsAccessKey == null || awsSecretKey == null) {
            System.err.println("Erro: 'aws.region', 'aws.s3.access-key' ou 'aws.s3.secret-key' não encontrados no application.properties.");
            System.exit(1);
        }

        System.out.println("Região configurada: " + awsRegionString);
        Region region = Region.of(awsRegionString);

        AwsBasicCredentials credentials = AwsBasicCredentials.create(awsAccessKey, awsSecretKey);
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);

        SqsClient sqsClient = null;
        try {
            sqsClient = SqsClient.builder()
                    .region(region)
                    .credentialsProvider(credentialsProvider)
                    .build();

            listQueues(sqsClient);

        } catch (SqsException e) {
            System.err.println("Erro SQS ao listar filas: " + e.awsErrorDetails().errorMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Erro geral: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } finally {
            if (sqsClient != null) {
                sqsClient.close();
                System.out.println("SQS Client fechado.");
            }
        }
    }

    public static void listQueues(SqsClient sqsClient) {
        System.out.println("Listando filas SQS...");
        try {
            ListQueuesRequest listQueuesRequest = ListQueuesRequest.builder().build();
            ListQueuesResponse listQueuesResponse = sqsClient.listQueues(listQueuesRequest);

            if (listQueuesResponse.queueUrls().isEmpty()) {
                System.out.println("Nenhuma fila SQS encontrada.");
            } else {
                listQueuesResponse.queueUrls().forEach(queueUrl ->
                        System.out.println(" URL da Fila: " + queueUrl.toLowerCase())
                );
            }
        } catch (SqsException e) {
            throw e;
        }
    }
}
