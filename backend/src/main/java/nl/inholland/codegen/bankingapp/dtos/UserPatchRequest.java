package nl.inholland.codegen.bankingapp.dtos;

public record UserPatchRequest (
    long userId,
    String firstName,
    String lastName)
{}
